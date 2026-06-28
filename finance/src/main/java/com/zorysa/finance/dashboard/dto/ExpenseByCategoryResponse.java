package com.zorysa.finance.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseByCategoryResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal amount
) {
}
