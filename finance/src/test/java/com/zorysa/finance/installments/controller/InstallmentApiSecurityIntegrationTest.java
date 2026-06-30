package com.zorysa.finance.installments.controller;

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
class InstallmentApiSecurityIntegrationTest {

    private static final UUID CARD_ID = UUID.fromString("25252525-2525-2525-2525-252525252525");
    private static final UUID PURCHASE_ID = UUID.fromString("26262626-2626-2626-2626-262626262626");
    private static final UUID CATEGORY_ID = UUID.fromString("27272727-2727-2727-2727-272727272727");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForCreatingAndListingPurchases() throws Exception {
        mockMvc.perform(post("/api/credit-cards/{cardId}/purchases", CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Notebook",
                                  "categoryId": "%s",
                                  "totalAmount": 3000.00,
                                  "purchaseDate": "2026-06-20",
                                  "installmentCount": 10,
                                  "notes": ""
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/credit-cards/{cardId}/purchases", CARD_ID)
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "ACTIVE")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForPurchaseDetailsUpdateAndDelete() throws Exception {
        mockMvc.perform(get("/api/card-purchases/{purchaseId}", PURCHASE_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/card-purchases/{purchaseId}", PURCHASE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Notebook Pro",
                                  "categoryId": "%s",
                                  "totalAmount": 3200.00,
                                  "purchaseDate": "2026-06-20",
                                  "installmentCount": 10,
                                  "notes": "Atualizado"
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/card-purchases/{purchaseId}", PURCHASE_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForListingInstallments() throws Exception {
        mockMvc.perform(get("/api/installments")
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "OPEN")
                        .param("cardId", CARD_ID.toString())
                        .param("month", "6")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/installments/future")
                        .param("page", "0")
                        .param("size", "20")
                        .param("cardId", CARD_ID.toString())
                        .param("fromMonth", "6")
                        .param("fromYear", "2026"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/card-purchases/{purchaseId}/installments", PURCHASE_ID)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isUnauthorized());
    }
}
