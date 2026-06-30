package com.zorysa.finance.reports.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseByCategoryReportResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal totalAmount,
        BigDecimal percentage
) {
}
