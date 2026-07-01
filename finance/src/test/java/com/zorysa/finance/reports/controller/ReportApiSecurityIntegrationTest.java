package com.zorysa.finance.reports.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zorysa.finance.auth.repository.PasswordResetTokenRepository;
import com.zorysa.finance.auth.repository.RefreshTokenRepository;
import com.zorysa.finance.users.repository.UserRepository;
import java.util.UUID;
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
class ReportApiSecurityIntegrationTest {

    private static final UUID CATEGORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ACCOUNT_ID = UUID.fromString("34343434-3434-3434-3434-343434343434");
    private static final UUID CARD_ID = UUID.fromString("35353535-3535-3535-3535-353535353535");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForTransactionAndCategoryReports() throws Exception {
        mockMvc.perform(get("/api/reports/transactions")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "transactionDate,desc")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30")
                        .param("type", "EXPENSE")
                        .param("categoryId", CATEGORY_ID.toString())
                        .param("accountId", ACCOUNT_ID.toString()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reports/expenses-by-category")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "totalAmount,desc")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForEvolutionAccountAndBudgetReports() throws Exception {
        mockMvc.perform(get("/api/reports/monthly-evolution")
                        .param("page", "0")
                        .param("size", "12")
                        .param("sort", "month,asc")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reports/accounts-balance")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "accountName,asc")
                        .param("date", "2026-06-30"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reports/budget-vs-actual")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "percentageUsed,desc")
                        .param("month", "6")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCardAndFutureInstallmentReports() throws Exception {
        mockMvc.perform(get("/api/reports/credit-card-expenses")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "totalAmount,desc")
                        .param("cardId", CARD_ID.toString())
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reports/future-installments")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "competenceYear,asc")
                        .param("cardId", CARD_ID.toString())
                        .param("fromMonth", "6")
                        .param("fromYear", "2026")
                        .param("toMonth", "12")
                        .param("toYear", "2026"))
                .andExpect(status().isUnauthorized());
    }
}
