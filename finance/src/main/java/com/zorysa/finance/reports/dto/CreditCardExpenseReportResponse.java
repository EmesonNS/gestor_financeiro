package com.zorysa.finance.reports.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardExpenseReportResponse(
        UUID cardId,
        String cardName,
        UUID categoryId,
        String categoryName,
        BigDecimal totalAmount
) {
}
