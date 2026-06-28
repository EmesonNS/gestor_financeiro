package com.zorysa.finance.accounts.dto;

import com.zorysa.finance.accounts.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull AccountType type,
        @NotNull @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal initialBalance
) {
}
