package com.zorysa.finance.accounts.controller;

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
class AccountApiSecurityIntegrationTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationForListingAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreatingAccount() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nubank",
                                  "type": "DIGITAL_ACCOUNT",
                                  "initialBalance": 1000.00
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForReadingUpdatingArchivingAndDeletingAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/accounts/{id}", ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Conta principal",
                                  "type": "CHECKING_ACCOUNT"
                                }
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/accounts/{id}/archive", ACCOUNT_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isUnauthorized());
    }
}
