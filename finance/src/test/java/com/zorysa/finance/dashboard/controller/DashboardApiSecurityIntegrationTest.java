package com.zorysa.finance.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zorysa.finance.auth.repository.PasswordResetTokenRepository;
import com.zorysa.finance.auth.repository.RefreshTokenRepository;
import com.zorysa.finance.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DashboardApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .param("month", "6")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForMonthlyDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/monthly")
                        .param("month", "6")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForDashboardCharts() throws Exception {
        mockMvc.perform(get("/api/dashboard/charts/expenses-by-category")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "amount,desc")
                        .param("month", "6")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/dashboard/charts/income-vs-expense")
                        .param("page", "0")
                        .param("size", "12")
                        .param("sort", "month,asc")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
    }
}
