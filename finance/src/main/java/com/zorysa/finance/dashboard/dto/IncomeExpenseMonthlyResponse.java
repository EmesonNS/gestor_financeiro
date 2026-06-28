package com.zorysa.finance.dashboard.dto;

import java.math.BigDecimal;

public record IncomeExpenseMonthlyResponse(
        int month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}
