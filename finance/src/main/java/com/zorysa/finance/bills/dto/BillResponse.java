package com.zorysa.finance.bills.dto;

import com.zorysa.finance.bills.entity.BillStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BillResponse(
        UUID id,
        String description,
        BigDecimal amount,
        LocalDate dueDate,
        UUID categoryId,
        UUID accountId,
        BillStatus status,
        LocalDate paidAt,
        UUID transactionId,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt
) {
}
