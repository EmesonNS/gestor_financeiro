package com.zorysa.finance.reports.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetVsActualReportResponse(
        UUID budgetId,
        UUID categoryId,
        String categoryName,
        BigDecimal plannedAmount,
        BigDecimal actualAmount,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        boolean exceeded
) {
}
