package com.zorysa.finance.installments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateCardPurchaseRequest(
        @NotBlank String description,
        @NotNull UUID categoryId,
        @NotNull @Positive BigDecimal totalAmount,
        @NotNull LocalDate purchaseDate,
        @NotNull @Positive Integer installmentCount,
        String notes
) {
}
