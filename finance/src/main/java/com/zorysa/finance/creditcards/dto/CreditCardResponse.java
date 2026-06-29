package com.zorysa.finance.creditcards.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditCardResponse(
        UUID id,
        String name,
        BigDecimal limitAmount,
        BigDecimal usedLimit,
        BigDecimal availableLimit,
        int closingDay,
        int dueDay,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
