package com.zorysa.finance.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zorysa.finance.auth.dto.AuthTokenResponse;
import com.zorysa.finance.auth.dto.ForgotPasswordRequest;
import com.zorysa.finance.auth.dto.LoginRequest;
import com.zorysa.finance.auth.dto.RefreshTokenRequest;
import com.zorysa.finance.auth.dto.RegisterRequest;
import com.zorysa.finance.auth.dto.ResetPasswordRequest;
import com.zorysa.finance.auth.entity.PasswordResetToken;
import com.zorysa.finance.auth.entity.RefreshToken;
import com.zorysa.finance.auth.repository.PasswordResetTokenRepository;
import com.zorysa.finance.auth.repository.RefreshTokenRepository;
import com.zorysa.finance.auth.security.JwtProperties;
import com.zorysa.finance.auth.security.JwtService;
import com.zorysa.finance.shared.exception.UnauthorizedException;
import com.zorysa.finance.users.dto.UserResponse;
import com.zorysa.finance.users.entity.User;
import com.zorysa.finance.users.service.UserService;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-25T10:00:00Z");
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private TokenHashService tokenHashService;

    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties("test-secret-with-at-least-32-bytes", 900, 604800, 3600);
        authService = new AuthService(
                userService,
                passwordEncoder,
                jwtService,
                jwtProperties,
                refreshTokenRepository,
                passwordResetTokenRepository,
                tokenHashService,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        user = new User("Maria", "maria@email.com", "stored-password-hash");
        ReflectionTestUtils.setField(user, "id", USER_ID);
        ReflectionTestUtils.setField(user, "createdAt", NOW.minusSeconds(60));
    }

    @Test
    void shouldRegisterUserAsPendingApprovalAndReturnApprovalMessage() {
        RegisterRequest request = new RegisterRequest("Maria", "maria@email.com", "secret123");
        UserResponse expectedResponse = new UserResponse(USER_ID, "Maria", "maria@email.com", NOW.minusSeconds(60));
        when(userService.createUserResponse("Maria", "maria@email.com", "secret123")).thenReturn(expectedResponse);

        UserResponse response = authService.register(request);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(ReflectionUtils.findField(UserResponse.class, "status"))
                .as("UserResponse deve expor status do cadastro conforme contrato de /api/auth/register")
                .isNotNull();
        assertThat(ReflectionUtils.findField(UserResponse.class, "message"))
                .as("UserResponse deve expor mensagem de aprovação conforme contrato de /api/auth/register")
                .isNotNull();
        assertThat(response)
                .extracting("status", "message")
                .containsExactly("PENDING_APPROVAL", "Cadastro enviado para aprovação.");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldLoginWithValidCredentialsAndIssueAccessAndRefreshTokens() {
        when(userService.findActiveByEmail("maria@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "stored-password-hash")).thenReturn(true);
        when(jwtService.createAccessToken(USER_ID, "maria@email.com")).thenReturn("access-token");
        when(jwtService.accessTokenExpirationSeconds()).thenReturn(900L);
        when(tokenHashService.generateOpaqueToken()).thenReturn("refresh-token");
        when(tokenHashService.sha256("refresh-token")).thenReturn("refresh-token-hash");

        AuthTokenResponse response = authService.login(new LoginRequest("maria@email.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.expiresIn()).isEqualTo(900);

        ArgumentCaptor<RefreshToken> refreshToken = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshToken.capture());
        assertThat(refreshToken.getValue().getUser()).isEqualTo(user);
        assertThat(refreshToken.getValue().isUsable(NOW)).isTrue();
        assertThat(refreshToken.getValue().isUsable(NOW.plusSeconds(604800))).isFalse();
    }

    @Test
    void shouldRejectLoginWhenUserDoesNotExistOrIsInactive() {
        when(userService.findActiveByEmail("maria@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("maria@email.com", "secret123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais invalidas");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRejectLoginWithWrongPassword() {
        when(userService.findActiveByEmail("maria@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "stored-password-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("maria@email.com", "wrong")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais invalidas");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRejectLoginWhenUserIsPendingApproval() {
        setAdministrativeStatus(user, "PENDING_APPROVAL");
        when(userService.findActiveByEmail("maria@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "stored-password-hash")).thenReturn(true);

        Throwable error = catchThrowable(() -> authService.login(new LoginRequest("maria@email.com", "secret123")));

        assertAccountStatusDenied(error, "PENDING_APPROVAL", "ACCOUNT_PENDING_APPROVAL", "Sua conta está aguardando aprovação.");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRejectLoginWhenUserIsRejectedSuspendedOrDeleted() {
        Map<String, String[]> expectedErrors = Map.of(
                "REJECTED", new String[]{"ACCOUNT_REJECTED", "Seu cadastro foi negado."},
                "SUSPENDED", new String[]{"ACCOUNT_SUSPENDED", "Sua conta está suspensa."},
                "DELETED", new String[]{"ACCOUNT_DELETED", "Esta conta não está disponível."}
        );

        expectedErrors.forEach((status, expected) -> {
            User blockedUser = userWithStatus(status);
            when(userService.findActiveByEmail(status.toLowerCase() + "@email.com")).thenReturn(Optional.of(blockedUser));
            when(passwordEncoder.matches("secret123", "stored-password-hash")).thenReturn(true);

            Throwable error = catchThrowable(() -> authService.login(new LoginRequest(status.toLowerCase() + "@email.com", "secret123")));

            assertAccountStatusDenied(error, status, expected[0], expected[1]);
        });

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRotateUsableRefreshTokenAndIssueNewSessionTokens() {
        RefreshToken currentRefreshToken = new RefreshToken(user, "old-refresh-token-hash", NOW.plusSeconds(60));
        when(tokenHashService.sha256("old-refresh-token")).thenReturn("old-refresh-token-hash");
        when(refreshTokenRepository.findByTokenHash("old-refresh-token-hash")).thenReturn(Optional.of(currentRefreshToken));
        when(jwtService.createAccessToken(USER_ID, "maria@email.com")).thenReturn("new-access-token");
        when(jwtService.accessTokenExpirationSeconds()).thenReturn(900L);
        when(tokenHashService.generateOpaqueToken()).thenReturn("new-refresh-token");
        when(tokenHashService.sha256("new-refresh-token")).thenReturn("new-refresh-token-hash");

        AuthTokenResponse response = authService.refresh(new RefreshTokenRequest("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.expiresIn()).isEqualTo(900);
        assertThat(currentRefreshToken.isUsable(NOW)).isFalse();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectMissingExpiredOrRevokedRefreshToken() {
        RefreshToken expiredRefreshToken = new RefreshToken(user, "old-refresh-token-hash", NOW.minusSeconds(1));
        when(tokenHashService.sha256("old-refresh-token")).thenReturn("old-refresh-token-hash");
        when(refreshTokenRepository.findByTokenHash("old-refresh-token-hash")).thenReturn(Optional.of(expiredRefreshToken));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("old-refresh-token")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token invalido");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldLogoutByRevokingRefreshTokenWhenItExists() {
        RefreshToken refreshToken = new RefreshToken(user, "refresh-token-hash", NOW.plusSeconds(60));
        when(tokenHashService.sha256("refresh-token")).thenReturn("refresh-token-hash");
        when(refreshTokenRepository.findByTokenHash("refresh-token-hash")).thenReturn(Optional.of(refreshToken));

        authService.logout("refresh-token");

        assertThat(refreshToken.isUsable(NOW)).isFalse();
    }

    @Test
    void shouldIgnoreLogoutWhenRefreshTokenDoesNotExist() {
        when(tokenHashService.sha256("unknown-refresh-token")).thenReturn("unknown-refresh-token-hash");
        when(refreshTokenRepository.findByTokenHash("unknown-refresh-token-hash")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.logout("unknown-refresh-token")).doesNotThrowAnyException();
    }

    @Test
    void shouldCreatePasswordResetTokenWhenEmailBelongsToActiveUser() {
        when(userService.findActiveByEmail("maria@email.com")).thenReturn(Optional.of(user));
        when(tokenHashService.generateOpaqueToken()).thenReturn("reset-token");
        when(tokenHashService.sha256("reset-token")).thenReturn("reset-token-hash");

        authService.forgotPassword(new ForgotPasswordRequest("maria@email.com"));

        ArgumentCaptor<PasswordResetToken> resetToken = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(resetToken.capture());
        assertThat(resetToken.getValue().getUser()).isEqualTo(user);
        assertThat(resetToken.getValue().isUsable(NOW)).isTrue();
        assertThat(resetToken.getValue().isUsable(NOW.plusSeconds(3600))).isFalse();
    }

    @Test
    void shouldNotRevealWhetherForgotPasswordEmailExists() {
        when(userService.findActiveByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.forgotPassword(new ForgotPasswordRequest("unknown@email.com")))
                .doesNotThrowAnyException();

        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void shouldResetPasswordWithUsableResetTokenAndMarkTokenAsUsed() {
        PasswordResetToken resetToken = new PasswordResetToken(user, "reset-token-hash", NOW.plusSeconds(60));
        when(tokenHashService.sha256("reset-token")).thenReturn("reset-token-hash");
        when(passwordResetTokenRepository.findByTokenHash("reset-token-hash")).thenReturn(Optional.of(resetToken));
        authService.resetPassword(new ResetPasswordRequest("reset-token", "new-secret123"));

        assertThat(resetToken.isUsable(NOW)).isFalse();
        verify(userService).updatePassword(user, "new-secret123");
    }

    @Test
    void shouldRejectInvalidOrExpiredPasswordResetToken() {
        PasswordResetToken expiredResetToken = new PasswordResetToken(user, "reset-token-hash", NOW.minusSeconds(1));
        when(tokenHashService.sha256("reset-token")).thenReturn("reset-token-hash");
        when(passwordResetTokenRepository.findByTokenHash("reset-token-hash")).thenReturn(Optional.of(expiredResetToken));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("reset-token", "new-secret123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token de redefinicao invalido");

        verify(userService, never()).updatePassword(any(), any());
    }

    private void assertAccountStatusDenied(Throwable error, String userStatus, String code, String message) {
        assertThat(error)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(message);
        assertThat(readStringMember(error, "code"))
                .as("excecao de bloqueio administrativo deve expor code para o front-end")
                .isEqualTo(code);
        assertThat(readStringMember(error, "userStatus"))
                .as("excecao de bloqueio administrativo deve expor userStatus para o front-end")
                .isEqualTo(userStatus);
    }

    private String readStringMember(Throwable error, String name) {
        try {
            return (String) error.getClass().getMethod(name).invoke(error);
        } catch (ReflectiveOperationException ignored) {
            try {
                return (String) error.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1)).invoke(error);
            } catch (ReflectiveOperationException exception) {
                Field field = ReflectionUtils.findField(error.getClass(), name);
                assertThat(field)
                        .as("excecao de bloqueio administrativo deve expor " + name)
                        .isNotNull();
                ReflectionUtils.makeAccessible(field);
                return (String) ReflectionUtils.getField(field, error);
            }
        }
    }

    private User userWithStatus(String status) {
        User blockedUser = new User("Maria", status.toLowerCase() + "@email.com", "stored-password-hash");
        ReflectionTestUtils.setField(blockedUser, "id", UUID.nameUUIDFromBytes(status.getBytes()));
        setAdministrativeStatus(blockedUser, status);
        return blockedUser;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setAdministrativeStatus(User target, String status) {
        Field field = ReflectionUtils.findField(User.class, "status");
        assertThat(field)
                .as("User deve expor o status administrativo exigido pelos requisitos de autenticação")
                .isNotNull();
        Object value = field.getType().isEnum() ? Enum.valueOf((Class<? extends Enum>) field.getType(), status) : status;
        ReflectionTestUtils.setField(target, "status", value);
    }
}
