package com.zorysa.finance.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal totalBalance,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpense,
        BigDecimal monthlyBalance,
        BigDecimal expectedBalance,
        BigDecimal openInvoicesTotal,
        BigDecimal currentInvoiceAmount,
        BigDecimal creditLimitUsed
) {
}
