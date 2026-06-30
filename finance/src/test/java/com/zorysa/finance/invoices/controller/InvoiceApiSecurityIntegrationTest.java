package com.zorysa.finance.invoices.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class InvoiceApiSecurityIntegrationTest {

    private static final UUID CARD_ID = UUID.fromString("18181818-1818-1818-1818-181818181818");
    private static final UUID INVOICE_ID = UUID.fromString("19191919-1919-1919-1919-191919191919");
    private static final UUID ACCOUNT_ID = UUID.fromString("20202020-2020-2020-2020-202020202020");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingInvoices() throws Exception {
        mockMvc.perform(get("/api/credit-cards/{cardId}/invoices", CARD_ID)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "referenceYear,desc")
                        .param("status", "OPEN")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCurrentInvoiceAndDetails() throws Exception {
        mockMvc.perform(get("/api/credit-cards/{cardId}/invoices/current", CARD_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/invoices/{invoiceId}", INVOICE_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForPayingInvoice() throws Exception {
        mockMvc.perform(patch("/api/invoices/{invoiceId}/pay", INVOICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentAccountId": "%s",
                                  "paidAt": "2026-06-20"
                                }
                                """.formatted(ACCOUNT_ID)))
                .andExpect(status().isUnauthorized());
    }
}
