package com.zorysa.finance.users.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Instant createdAt,
        String status,
        String message
) {
    private static final String DEFAULT_STATUS = "PENDING_APPROVAL";
    private static final String DEFAULT_MESSAGE = "Cadastro enviado para aprovação.";

    public UserResponse(UUID id, String name, String email, Instant createdAt) {
        this(id, name, email, createdAt, DEFAULT_STATUS, DEFAULT_MESSAGE);
    }
}
