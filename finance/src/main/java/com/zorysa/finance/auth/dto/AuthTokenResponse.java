package com.zorysa.finance.auth.dto;

import java.util.UUID;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        AuthenticatedUserResponse user
) {
    public AuthTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this(
                accessToken,
                refreshToken,
                expiresIn,
                new AuthenticatedUserResponse(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "Maria",
                        "maria@email.com",
                        "USER"
                )
        );
    }
}
