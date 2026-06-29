package com.zorysa.finance.budgets.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateBudgetRequest(
        @NotNull UUID categoryId,
        @NotNull @Min(1) @Max(12) Integer startMonth,
        @NotNull Integer startYear,
        @Min(1) @Max(12) Integer endMonth,
        Integer endYear,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 13, fraction = 2) BigDecimal limitAmount
) {

    @AssertTrue(message = "endMonth e endYear devem ser ambos nulos ou ambos preenchidos, e o fim deve ser igual ou posterior ao inicio")
    public boolean isEndPeriodValid() {
        boolean endMissing = endMonth == null && endYear == null;
        boolean endComplete = endMonth != null && endYear != null;
        if (endMissing) {
            return true;
        }
        if (!endComplete || startMonth == null || startYear == null) {
            return false;
        }
        return endYear > startYear || endYear.equals(startYear) && endMonth >= startMonth;
    }
}
