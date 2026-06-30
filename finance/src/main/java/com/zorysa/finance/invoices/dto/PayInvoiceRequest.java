package com.zorysa.finance.invoices.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record PayInvoiceRequest(
        @NotNull UUID paymentAccountId,
        @NotNull LocalDate paidAt
) {
}
