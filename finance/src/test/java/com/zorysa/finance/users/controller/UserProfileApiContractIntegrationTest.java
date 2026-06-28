package com.zorysa.finance.users.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zorysa.finance.auth.repository.PasswordResetTokenRepository;
import com.zorysa.finance.auth.repository.RefreshTokenRepository;
import com.zorysa.finance.auth.security.AuthUserPrincipal;
import com.zorysa.finance.users.repository.UserRepository;
import com.zorysa.finance.users.service.UserService;
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
class UserProfileApiContractIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequireAuthenticationToReadProfile() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAuthenticatedUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/me").with(user(principal())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateAuthenticatedUserName() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectProfileUpdateWithoutName() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldChangeAuthenticatedUserPassword() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-secret123",
                                  "newPassword": "new-secret123"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectPasswordChangeWithoutCurrentAndNewPassword() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "",
                                  "newPassword": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private AuthUserPrincipal principal() {
        return new AuthUserPrincipal(USER_ID, "maria@email.com", "stored-password-hash", "USER", true);
    }
}
