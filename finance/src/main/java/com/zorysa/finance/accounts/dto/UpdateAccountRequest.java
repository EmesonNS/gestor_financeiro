package com.zorysa.finance.accounts.dto;

import com.zorysa.finance.accounts.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull AccountType type
) {
}
