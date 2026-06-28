package com.zorysa.finance.accounts.dto;

import com.zorysa.finance.accounts.entity.AccountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        AccountType type,
        BigDecimal initialBalance,
        BigDecimal currentBalance,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
