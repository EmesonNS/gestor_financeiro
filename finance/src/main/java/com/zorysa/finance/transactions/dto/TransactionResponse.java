package com.zorysa.finance.transactions.dto;

import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String description,
        BigDecimal amount,
        TransactionType type,
        LocalDate transactionDate,
        UUID categoryId,
        UUID accountId,
        TransactionStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
