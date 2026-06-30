package com.zorysa.finance.reports.dto;

import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionReportResponse(
        UUID transactionId,
        String description,
        TransactionType type,
        UUID categoryId,
        String categoryName,
        UUID accountId,
        String accountName,
        BigDecimal amount,
        LocalDate transactionDate,
        TransactionStatus status
) {
}
