package com.travel.expense_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.expense_management.dto.auth.LoginRequest;
import com.travel.expense_management.dto.auth.RegisterRequest;
import com.travel.expense_management.dto.expense.ExpenseRequest;
import com.travel.expense_management.dto.trip.TripRequest;
import com.travel.expense_management.entity.ExpenseCategory;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class TripAndExpenseIntegrationTest {

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
    void shouldPerformTripAndExpenseCrudSuccessfully() throws Exception {
        // 1. Register User 1
        RegisterRequest reg1 = new RegisterRequest("Employee One", "emp1@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg1)))
                .andExpect(status().isCreated());

        String emp1Token = obtainToken("emp1@example.com", "password123");

        // 2. Create Trip for User 1
        String tripRequestJson = String.format(
                "{\"destination\":\"Paris\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"budget\":1500.00,\"description\":\"Summer vacation in Paris\"}",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10)
        );

        String tripResponseString = mockMvc.perform(post("/trips")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.destination").value("Paris"))
                .andReturn().getResponse().getContentAsString();

        Integer tripIdInt = com.jayway.jsonpath.JsonPath.read(tripResponseString, "$.id");
        Long tripId = tripIdInt.longValue();

        // 3. Create Expense for Trip
        String expenseRequestJson = String.format(
                "{\"description\":\"Eiffel Tower Ticket\",\"amount\":50.00,\"category\":\"ACTIVITIES\",\"date\":\"%s\"}",
                LocalDate.now().plusDays(2)
        );

        String expenseResponseString = mockMvc.perform(post("/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.category").value("ACTIVITIES"))
                .andReturn().getResponse().getContentAsString();

        Integer expenseIdInt = com.jayway.jsonpath.JsonPath.read(expenseResponseString, "$.id");
        Long expenseId = expenseIdInt.longValue();

        // 4. Create User 2
        RegisterRequest reg2 = new RegisterRequest("Employee Two", "emp2@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isCreated());

        String emp2Token = obtainToken("emp2@example.com", "password123");

        // 5. User 2 should NOT be able to view User 1's Trip or Expense
        mockMvc.perform(get("/trips/" + tripId)
                        .header("Authorization", "Bearer " + emp2Token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + emp2Token))
                .andExpect(status().isForbidden());

        // 6. User 2 should NOT be able to add Expense to User 1's Trip
        mockMvc.perform(post("/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + emp2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestJson))
                .andExpect(status().isForbidden());

        // 7. Create Admin User manually
        User admin = User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("adminpass"))
                .role(Role.Admin)
                .build();
        userRepository.save(admin);

        String adminToken = obtainToken("admin@example.com", "adminpass");

        // 8. Admin User SHOULD be able to retrieve User 1's Trip and Expense
        mockMvc.perform(get("/trips/" + tripId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Paris"));

        mockMvc.perform(get("/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Eiffel Tower Ticket"));

        // 9. Fail to create Expense outside Trip date bounds
        String invalidDateExpenseJson = String.format(
                "{\"description\":\"Hotel booking\",\"amount\":200.00,\"category\":\"LODGING\",\"date\":\"%s\"}",
                LocalDate.now().minusDays(5)
        );

        mockMvc.perform(post("/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDateExpenseJson))
                .andExpect(status().isBadRequest());

        // 10. Fail to create Trip with invalid dates (end before start)
        String invalidTripJson = String.format(
                "{\"destination\":\"Rome\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"budget\":1000.00,\"description\":\"Invalid dates trip\"}",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(2)
        );

        mockMvc.perform(post("/trips")
                        .header("Authorization", "Bearer " + emp1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTripJson))
                .andExpect(status().isBadRequest());
    }
}
