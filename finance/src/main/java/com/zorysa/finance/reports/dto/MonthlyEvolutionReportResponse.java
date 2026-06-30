package com.zorysa.finance.reports.dto;

import java.math.BigDecimal;

public record MonthlyEvolutionReportResponse(
        int month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}
