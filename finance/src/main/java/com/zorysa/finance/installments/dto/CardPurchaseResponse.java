package com.zorysa.finance.installments.dto;

import com.zorysa.finance.installments.entity.PurchaseStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CardPurchaseResponse(
        UUID id,
        UUID creditCardId,
        UUID categoryId,
        String description,
        BigDecimal totalAmount,
        LocalDate purchaseDate,
        int installmentCount,
        PurchaseStatus status,
        String notes,
        List<InstallmentResponse> installments,
        Instant createdAt,
        Instant updatedAt
) {
}
