package com.travel.expense_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.expense_management.dto.auth.LoginRequest;
import com.travel.expense_management.dto.auth.RegisterRequest;
import com.travel.expense_management.dto.expense.ExpenseRequest;
import com.travel.expense_management.entity.ExpenseCategory;
import com.travel.expense_management.entity.Role;
import com.travel.expense_management.entity.User;
import com.travel.expense_management.repository.ExpenseRepository;
import com.travel.expense_management.repository.NotificationRepository;
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
public class PolicyAndReimbursementIntegrationTest {

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
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
        notificationRepository.deleteAll();
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
    void shouldEvaluatePolicyAndAllowReimbursementCorrectly() throws Exception {
        // 1. Create Employee and Manager
        RegisterRequest empReg = new RegisterRequest("Employee Policy", "emp_pol@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empReg)))
                .andExpect(status().isCreated());

        String empToken = obtainToken("emp_pol@example.com", "password123");

        User manager = User.builder()
                .fullName("Manager Policy")
                .email("manager_pol@example.com")
                .password(passwordEncoder.encode("managerpass"))
                .role(Role.Manager)
                .build();
        userRepository.save(manager);

        String managerToken = obtainToken("manager_pol@example.com", "managerpass");

        // 2. Create Trip as Employee with 10,000 INR budget
        String tripRequestJson = String.format(
                "{\"destination\":\"Mumbai\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"budget\":10000.00,\"description\":\"Mumbai Business trip\"}",
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(10)
        );

        String tripResponseString = mockMvc.perform(post("/trips")
                        .header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer tripIdInt = com.jayway.jsonpath.JsonPath.read(tripResponseString, "$.id");
        Long tripId = tripIdInt.longValue();

        // 3. Create normal compliant expense (Food = ₹2,000)
        String compliantExpenseJson = String.format(
                "{\"description\":\"Compliant Lunch\",\"amount\":2000.00,\"category\":\"FOOD\",\"date\":\"%s\"}",
                LocalDate.now().plusDays(3)
        );

        mockMvc.perform(post("/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compliantExpenseJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPolicyViolated").value(false));

        // 4. Create policy-violating food expense (Food = ₹6,000) - exceeds ₹5,000 category limit
        String violatingExpenseJson = String.format(
                "{\"description\":\"Lavish dinner\",\"amount\":6000.00,\"category\":\"FOOD\",\"date\":\"%s\"}",
                LocalDate.now().plusDays(4)
        );

        mockMvc.perform(post("/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(violatingExpenseJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPolicyViolated").value(true))
                .andExpect(jsonPath("$.policyViolationMessage").value(org.hamcrest.Matchers.containsString("Food expenses exceed")));

        // 5. Create budget-violating transport expense (Transport = ₹5,000) - total is now 2000 + 6000 + 5000 = 13000 > 10000 budget
        String budgetViolatingExpenseJson = String.format(
                "{\"description\":\"Taxi claim\",\"amount\":5000.00,\"category\":\"TRANSPORT\",\"date\":\"%s\"}",
                LocalDate.now().plusDays(5)
        );

        mockMvc.perform(post("/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(budgetViolatingExpenseJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPolicyViolated").value(true))
                .andExpect(jsonPath("$.policyViolationMessage").value(org.hamcrest.Matchers.containsString("exceed the allocated budget")));

        // 6. Manager approves the trip
        mockMvc.perform(put("/trips/" + tripId + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        // 7. Manager marks the trip as fully Reimbursed
        mockMvc.perform(put("/trips/" + tripId + "/reimburse")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REIMBURSED"));

        // 8. Verify Employee received a notification about reimbursement completion
        mockMvc.perform(get("/notifications")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value(org.hamcrest.Matchers.containsString("reimbursement for trip to Mumbai")));
    }
}
