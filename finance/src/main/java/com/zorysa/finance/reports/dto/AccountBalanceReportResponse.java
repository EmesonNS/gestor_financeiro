package com.zorysa.finance.reports.dto;

import com.zorysa.finance.accounts.entity.AccountType;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceReportResponse(
        UUID accountId,
        String accountName,
        AccountType accountType,
        BigDecimal balance
) {
}
