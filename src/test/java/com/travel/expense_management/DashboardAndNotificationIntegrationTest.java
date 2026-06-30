package com.travel.expense_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.expense_management.dto.auth.LoginRequest;
import com.travel.expense_management.dto.auth.RegisterRequest;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class DashboardAndNotificationIntegrationTest {

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
    void shouldCalculateMetricsAndNotifyCorrectly() throws Exception {
        // 1. Create Employee and Manager
        RegisterRequest empReg = new RegisterRequest("Employee Dash", "emp_dash@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empReg)))
                .andExpect(status().isCreated());

        String empToken = obtainToken("emp_dash@example.com", "password123");

        User manager = User.builder()
                .fullName("Manager Dash")
                .email("manager_dash@example.com")
                .password(passwordEncoder.encode("managerpass"))
                .role(Role.Manager)
                .build();
        userRepository.save(manager);

        String managerToken = obtainToken("manager_dash@example.com", "managerpass");

        // 2. Create Trip as Employee
        String tripRequestJson = String.format(
                "{\"destination\":\"Berlin\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"budget\":3000.00,\"description\":\"Berlin trip\"}",
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

        // 3. Verify Manager received a notification about the new trip submission
        mockMvc.perform(get("/notifications")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value(org.hamcrest.Matchers.containsString("New trip request to Berlin")));

        // 4. Check Employee metrics (should show 1 trip, $0 spent, 0% utilization)
        mockMvc.perform(get("/dashboard/metrics")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(1))
                .andExpect(jsonPath("$.totalSpent").value(0.0))
                .andExpect(jsonPath("$.budgetUtilizationPercentage").value(0.0));

        // 5. Manager approves the trip
        mockMvc.perform(put("/trips/" + tripId + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        // 6. Verify Employee received a notification about the approval
        String empNotifResponse = mockMvc.perform(get("/notifications")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value(org.hamcrest.Matchers.containsString("APPROVED")))
                .andReturn().getResponse().getContentAsString();

        Integer notifIdInt = com.jayway.jsonpath.JsonPath.read(empNotifResponse, "$[0].id");
        Long notifId = notifIdInt.longValue();

        // 7. Employee marks notification as read
        mockMvc.perform(put("/notifications/" + notifId + "/read")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk());

        // Verify the notification is marked as read
        mockMvc.perform(get("/notifications")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isRead").value(true));

        // 8. Verify reports CSV and summary endpoint works
        mockMvc.perform(get("/dashboard/reports/summary")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/reports/csv")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Expense ID,Trip ID")));
    }
}
