package com.zorysa.finance.dashboard.dto;

import java.math.BigDecimal;

public record DashboardMonthlyResponse(
        int month,
        int year,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance,
        BigDecimal expectedBalance
) {
}
