package com.zorysa.finance.bills.dto;

import com.zorysa.finance.bills.entity.BillStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBillRequest(
        @NotBlank @Size(max = 180) String description,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 13, fraction = 2) BigDecimal amount,
        @NotNull LocalDate dueDate,
        @NotNull UUID categoryId,
        UUID accountId,
        @NotNull BillStatus status
) {
}
