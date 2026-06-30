package com.zorysa.finance.installments.dto;

import com.zorysa.finance.installments.entity.InstallmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InstallmentResponse(
        UUID id,
        UUID purchaseId,
        UUID invoiceId,
        int installmentNumber,
        int totalInstallments,
        BigDecimal amount,
        int competenceMonth,
        int competenceYear,
        InstallmentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
