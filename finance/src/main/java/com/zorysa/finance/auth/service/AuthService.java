package com.zorysa.finance.auth.service;

import com.zorysa.finance.auth.dto.AuthTokenResponse;
import com.zorysa.finance.auth.dto.AuthenticatedUserResponse;
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
import com.zorysa.finance.shared.exception.AccountStatusAccessDeniedException;
import com.zorysa.finance.shared.exception.UnauthorizedException;
import com.zorysa.finance.users.dto.UserResponse;
import com.zorysa.finance.users.entity.User;
import com.zorysa.finance.users.service.UserService;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenHashService tokenHashService;
    private final Clock clock;

    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            TokenHashService tokenHashService,
            Clock clock
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenHashService = tokenHashService;
        this.clock = clock;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        return userService.createUserResponse(request.name(), request.email(), request.password());
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        User user = userService.findActiveByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Credenciais invalidas"));
        ensureUserCanLogin(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        Instant now = clock.instant();
        RefreshToken current = refreshTokenRepository.findByTokenHash(tokenHashService.sha256(request.refreshToken()))
                .filter(token -> token.isUsable(now))
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalido"));
        current.revoke(now);
        return issueTokens(current.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(tokenHashService.sha256(refreshToken))
                .ifPresent(token -> token.revoke(clock.instant()));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userService.findActiveByEmail(request.email()).ifPresent(user -> {
            String token = tokenHashService.generateOpaqueToken();
            PasswordResetToken resetToken = new PasswordResetToken(
                    user,
                    tokenHashService.sha256(token),
                    clock.instant().plusSeconds(jwtProperties.passwordResetExpirationSeconds())
            );
            passwordResetTokenRepository.save(resetToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = clock.instant();
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHashService.sha256(request.token()))
                .filter(found -> found.isUsable(now))
                .orElseThrow(() -> new UnauthorizedException("Token de redefinicao invalido"));
        userService.updatePassword(token.getUser(), request.newPassword());
        token.markUsed(now);
    }

    private AuthTokenResponse issueTokens(User user) {
        String accessToken = jwtService.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenHashService.generateOpaqueToken();
        RefreshToken entity = new RefreshToken(
                user,
                tokenHashService.sha256(refreshToken),
                clock.instant().plusSeconds(jwtProperties.refreshTokenExpirationSeconds())
        );
        refreshTokenRepository.save(entity);
        AuthenticatedUserResponse authenticatedUser = new AuthenticatedUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
        return new AuthTokenResponse(accessToken, refreshToken, jwtService.accessTokenExpirationSeconds(), authenticatedUser);
    }

    private void ensureUserCanLogin(User user) {
        if (user.isApproved()) {
            return;
        }
        switch (user.getStatus()) {
            case PENDING_APPROVAL -> throw accountStatusDenied(
                    "Sua conta está aguardando aprovação.",
                    "ACCOUNT_PENDING_APPROVAL",
                    "PENDING_APPROVAL"
            );
            case REJECTED -> throw accountStatusDenied(
                    "Seu cadastro foi negado.",
                    "ACCOUNT_REJECTED",
                    "REJECTED"
            );
            case SUSPENDED -> throw accountStatusDenied(
                    "Sua conta está suspensa.",
                    "ACCOUNT_SUSPENDED",
                    "SUSPENDED"
            );
            case DELETED -> throw accountStatusDenied(
                    "Esta conta não está disponível.",
                    "ACCOUNT_DELETED",
                    "DELETED"
            );
            case APPROVED -> {
            }
        }
    }

    private AccountStatusAccessDeniedException accountStatusDenied(String message, String code, String userStatus) {
        return new AccountStatusAccessDeniedException(message, code, userStatus);
    }
}
