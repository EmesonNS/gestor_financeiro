package com.zorysa.finance.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record UserStatusHistoryResponse(
        UUID id,
        UUID adminUserId,
        String previousStatus,
        String newStatus,
        String action,
        String reason,
        Instant createdAt
) {
}
