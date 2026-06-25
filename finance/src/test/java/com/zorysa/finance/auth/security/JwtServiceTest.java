package com.zorysa.finance.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tools.jackson.databind.ObjectMapper;
import com.zorysa.finance.shared.exception.UnauthorizedException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void shouldCreateAndValidateAccessToken() {
        JwtService jwtService = jwtServiceAt(Instant.parse("2026-06-25T10:00:00Z"), 900);

        String token = jwtService.createAccessToken(USER_ID, "maria@email.com");
        JwtSubject subject = jwtService.validateAccessToken(token);

        assertThat(subject.userId()).isEqualTo(USER_ID);
        assertThat(subject.email()).isEqualTo("maria@email.com");
    }

    @Test
    void shouldRejectExpiredAccessToken() {
        JwtService issuer = jwtServiceAt(Instant.parse("2026-06-25T10:00:00Z"), 60);
        String token = issuer.createAccessToken(USER_ID, "maria@email.com");
        JwtService validator = jwtServiceAt(Instant.parse("2026-06-25T10:02:00Z"), 60);

        assertThatThrownBy(() -> validator.validateAccessToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token expirado");
    }

    private JwtService jwtServiceAt(Instant instant, long expirationSeconds) {
        JwtProperties properties = new JwtProperties("test-secret-with-at-least-32-bytes", expirationSeconds, 604800, 3600);
        return new JwtService(properties, new ObjectMapper(), Clock.fixed(instant, ZoneOffset.UTC));
    }
}
