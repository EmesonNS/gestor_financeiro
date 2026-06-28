package com.zorysa.finance.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String name,
        String email,
        String role,
        String status,
        Instant createdAt,
        Instant approvedAt,
        Instant rejectedAt,
        Instant suspendedAt
) {
}
