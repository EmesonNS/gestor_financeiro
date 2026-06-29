package com.zorysa.finance.bills.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record PayBillRequest(
        @NotNull UUID accountId,
        @NotNull LocalDate paidAt
) {
}
