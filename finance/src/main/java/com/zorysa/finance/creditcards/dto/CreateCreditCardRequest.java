package com.zorysa.finance.creditcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateCreditCardRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal limitAmount,
        @NotNull @Min(1) @Max(31) Integer closingDay,
        @NotNull @Min(1) @Max(31) Integer dueDay
) {
}
