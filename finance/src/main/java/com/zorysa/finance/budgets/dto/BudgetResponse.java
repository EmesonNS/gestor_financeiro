package com.zorysa.finance.budgets.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        int startMonth,
        int startYear,
        Integer endMonth,
        Integer endYear,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal usagePercentage,
        boolean exceeded,
        Instant createdAt,
        Instant updatedAt
) {
}
