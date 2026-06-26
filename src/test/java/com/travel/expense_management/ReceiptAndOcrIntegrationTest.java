package com.travel.expense_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.expense_management.dto.auth.LoginRequest;
import com.travel.expense_management.dto.auth.RegisterRequest;
import com.travel.expense_management.entity.Role;
import com.travel.expense_management.entity.User;
import com.travel.expense_management.repository.ExpenseRepository;
import com.travel.expense_management.repository.TripRepository;
import com.travel.expense_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class ReceiptAndOcrIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
        expenseRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String obtainToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        String responseString = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(responseString, "$.accessToken");
    }

    @Test
    void shouldPerformReceiptUploadOcrAndDeletionSuccessfully() throws Exception {
        // 1. Register User 1
        RegisterRequest reg1 = new RegisterRequest("Employee One", "emp1@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg1)))
                .andExpect(status().isCreated());

        String emp1Token = obtainToken("emp1@example.com", "password123");

        // 2. Create Trip for User 1
        String tripRequestJson = String.format(
                "{\"destination\":\"Paris\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"budget\":1500.00,\"description\":\"Paris trip\"}",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5)
        );

        String tripResponseString = mockMvc.perform(post("/trips")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer tripIdInt = com.jayway.jsonpath.JsonPath.read(tripResponseString, "$.id");
        Long tripId = tripIdInt.longValue();

        // 3. Create Expense for Trip
        String expenseRequestJson = String.format(
                "{\"description\":\"Lunch\",\"amount\":10.00,\"category\":\"FOOD\",\"date\":\"%s\"}",
                LocalDate.now()
        );

        String expenseResponseString = mockMvc.perform(post("/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer expenseIdInt = com.jayway.jsonpath.JsonPath.read(expenseResponseString, "$.id");
        Long expenseId = expenseIdInt.longValue();

        // 4. Prepare Mock Receipt MultipartFile
        byte[] dummyContent = "dummy image content".getBytes();
        MockMultipartFile receiptFile = new MockMultipartFile(
                "file",
                "starbucks_receipt.png",
                "image/png",
                dummyContent
        );

        // 5. Upload receipt and verify OCR autofill/overwrite details
        mockMvc.perform(multipart("/expenses/" + expenseId + "/receipt")
                        .file(receiptFile)
                        .header("Authorization", "Bearer " + emp1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId", notNullValue()))
                .andExpect(jsonPath("$.receiptFileName").value("starbucks_receipt.png"))
                // Verify OCR fields prefilled
                .andExpect(jsonPath("$.description").value("Starbucks Coffee"))
                .andExpect(jsonPath("$.amount").value(12.50))
                .andExpect(jsonPath("$.category").value("FOOD"));

        // 6. Download receipt and verify bytes and content type
        mockMvc.perform(get("/expenses/" + expenseId + "/receipt")
                        .header("Authorization", "Bearer " + emp1Token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(dummyContent));

        // 7. Register User 2
        RegisterRequest reg2 = new RegisterRequest("Employee Two", "emp2@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isCreated());

        String emp2Token = obtainToken("emp2@example.com", "password123");

        // 8. User 2 should NOT be able to view or delete User 1's receipt
        mockMvc.perform(get("/expenses/" + expenseId + "/receipt")
                        .header("Authorization", "Bearer " + emp2Token))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/expenses/" + expenseId + "/receipt")
                        .header("Authorization", "Bearer " + emp2Token))
                .andExpect(status().isForbidden());

        // 9. Fail to upload invalid file format (text file)
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "receipt.txt",
                "text/plain",
                "invalid format content".getBytes()
        );

        mockMvc.perform(multipart("/expenses/" + expenseId + "/receipt")
                        .file(txtFile)
                        .header("Authorization", "Bearer " + emp1Token))
                .andExpect(status().isBadRequest());

        // 10. Admin user SHOULD be able to download the receipt
        User admin = User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("adminpass"))
                .role(Role.Admin)
                .build();
        userRepository.save(admin);

        String adminToken = obtainToken("admin@example.com", "adminpass");

        mockMvc.perform(get("/expenses/" + expenseId + "/receipt")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        // 11. Delete receipt and verify dissociation
        mockMvc.perform(delete("/expenses/" + expenseId + "/receipt")
                        .header("Authorization", "Bearer " + emp1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId").value((Object) null))
                .andExpect(jsonPath("$.receiptFileName").value((Object) null));
    }
}
