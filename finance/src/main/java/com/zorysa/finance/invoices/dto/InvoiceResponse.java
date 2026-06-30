package com.zorysa.finance.invoices.dto;

import com.zorysa.finance.invoices.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID creditCardId,
        int referenceMonth,
        int referenceYear,
        LocalDate closingDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        InvoiceStatus status,
        LocalDate paidAt,
        UUID paymentAccountId,
        Instant createdAt,
        Instant updatedAt
) {
}
