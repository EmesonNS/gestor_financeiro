package com.zorysa.finance.auth.controller;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zorysa.finance.auth.dto.AuthTokenResponse;
import com.zorysa.finance.auth.dto.LoginRequest;
import com.zorysa.finance.auth.dto.RefreshTokenRequest;
import com.zorysa.finance.auth.dto.RegisterRequest;
import com.zorysa.finance.auth.repository.PasswordResetTokenRepository;
import com.zorysa.finance.auth.repository.RefreshTokenRepository;
import com.zorysa.finance.auth.service.AuthService;
import com.zorysa.finance.users.dto.UserResponse;
import com.zorysa.finance.users.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiContractIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void shouldRequestRegistrationAndReturnPendingApprovalWithoutTokens() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse(USER_ID, "Maria", "maria@email.com", Instant.parse("2026-06-25T10:00:00Z")));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"name\": \"Maria\",
                                  \"email\": \"maria@email.com\",
                                  \"password\": \"secret123\"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.message").value("Cadastro enviado para aprovação."))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void shouldRejectInvalidRegistrationPayload() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"name\": \"\",
                                  \"email\": \"invalid\",
                                  \"password\": \"\"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginAndReturnAuthenticatedSessionTokens() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthTokenResponse("access-token", "refresh-token", 900));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"email\": \"maria@email.com\",
                                  \"password\": \"secret123\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.user.name").value("Maria"))
                .andExpect(jsonPath("$.user.email").value("maria@email.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @ParameterizedTest
    @CsvSource({
            "PENDING_APPROVAL, ACCOUNT_PENDING_APPROVAL, Sua conta está aguardando aprovação.",
            "SUSPENDED, ACCOUNT_SUSPENDED, Sua conta está suspensa.",
            "REJECTED, ACCOUNT_REJECTED, Seu cadastro foi negado.",
            "DELETED, ACCOUNT_DELETED, Esta conta não está disponível."
    })
    void shouldReturnForbiddenWithCodeAndUserStatusWhenLoginUserIsBlocked(String userStatus, String code, String message) throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AccountStatusAccessDeniedException(message, code, userStatus));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"email\": \"maria@email.com\",
                                  \"password\": \"secret123\"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.userStatus").value(userStatus));
    }

    @Test
    void shouldRejectInvalidLoginPayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"email\": \"invalid\",
                                  \"password\": \"\"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRequireAuthenticationToLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"refreshToken\": \"refresh-token\"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutAuthenticatedUserAndInvalidateRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(user("maria@email.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"refreshToken\": \"refresh-token\"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(authService).logout(any());
    }

    @Test
    void shouldRefreshAccessTokenAndRotateRefreshToken() throws Exception {
        when(authService.refresh(any(RefreshTokenRequest.class)))
                .thenReturn(new AuthTokenResponse("new-access-token", "new-refresh-token", 900));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"refreshToken\": \"refresh-token\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void shouldAcceptForgotPasswordRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"email\": \"maria@email.com\"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(authService).forgotPassword(any());
    }

    @Test
    void shouldAcceptResetPasswordWithTokenWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"token\": \"reset-token\",
                                  \"newPassword\": \"new-secret123\"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(authService).resetPassword(any());
    }
    private static class AccountStatusAccessDeniedException extends AccessDeniedException {

        private final String code;
        private final String userStatus;

        AccountStatusAccessDeniedException(String message, String code, String userStatus) {
            super(message);
            this.code = code;
            this.userStatus = userStatus;
        }

        public String getCode() {
            return code;
        }

        public String getUserStatus() {
            return userStatus;
        }
    }
}
