package com.zorysa.finance.goals.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateGoalProgressRequest(
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 13, fraction = 2) BigDecimal currentAmount
) {
}
