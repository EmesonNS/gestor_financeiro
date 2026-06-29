package com.zorysa.finance.budgets.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zorysa.finance.auth.repository.PasswordResetTokenRepository;
import com.zorysa.finance.auth.repository.RefreshTokenRepository;
import com.zorysa.finance.users.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BudgetApiSecurityIntegrationTest {

    private static final UUID BUDGET_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID CATEGORY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingBudgets() throws Exception {
        mockMvc.perform(get("/api/budgets")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "startYear,desc")
                        .param("month", "6")
                        .param("year", "2026")
                        .param("categoryId", CATEGORY_ID.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreatingBudget() throws Exception {
        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "startMonth": 6,
                                  "startYear": 2026,
                                  "endMonth": null,
                                  "endYear": null,
                                  "limitAmount": 1000.00
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForReadingUpdatingAndDeletingBudget() throws Exception {
        mockMvc.perform(get("/api/budgets/{id}", BUDGET_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/budgets/{id}", BUDGET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "startMonth": 6,
                                  "startYear": 2026,
                                  "endMonth": 12,
                                  "endYear": 2026,
                                  "limitAmount": 1200.00
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/budgets/{id}", BUDGET_ID))
                .andExpect(status().isUnauthorized());
    }
}
