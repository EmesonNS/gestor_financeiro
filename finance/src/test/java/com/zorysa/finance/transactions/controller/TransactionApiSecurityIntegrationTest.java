package com.zorysa.finance.transactions.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class TransactionApiSecurityIntegrationTest {

    private static final UUID TRANSACTION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID CATEGORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ACCOUNT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "transactionDate,desc")
                        .param("type", "EXPENSE")
                        .param("status", "PAID"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreatingTransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForReadingUpdatingDeletingPayingAndCancelingTransaction() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/transactions/{id}", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/transactions/{id}/mark-as-paid", TRANSACTION_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/transactions/{id}/cancel", TRANSACTION_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isUnauthorized());
    }

    private String requestBody() {
        return """
                {
                  "description": "Mercado",
                  "amount": 150.25,
                  "type": "EXPENSE",
                  "transactionDate": "2026-06-20",
                  "categoryId": "%s",
                  "accountId": "%s",
                  "status": "PAID",
                  "notes": ""
                }
                """.formatted(CATEGORY_ID, ACCOUNT_ID);
    }
}
