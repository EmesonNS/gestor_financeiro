package com.zorysa.finance.creditcards.controller;

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
class CreditCardApiSecurityIntegrationTest {

    private static final UUID CARD_ID = UUID.fromString("15151515-1515-1515-1515-151515151515");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingCreditCards() throws Exception {
        mockMvc.perform(get("/api/credit-cards")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "name,asc")
                        .param("archived", "false"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreatingCreditCard() throws Exception {
        mockMvc.perform(post("/api/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Visa",
                                  "limitAmount": 5000.00,
                                  "closingDay": 10,
                                  "dueDay": 17
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForReadingUpdatingDeletingAndArchivingCreditCard() throws Exception {
        mockMvc.perform(get("/api/credit-cards/{id}", CARD_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/credit-cards/{id}", CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Visa Platinum",
                                  "limitAmount": 7500.00,
                                  "closingDay": 10,
                                  "dueDay": 17
                                }
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/credit-cards/{id}/archive", CARD_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/credit-cards/{id}", CARD_ID))
                .andExpect(status().isUnauthorized());
    }
}
