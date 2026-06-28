package com.zorysa.finance.transactions.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MarkTransactionAsPaidRequest(
        UUID accountId,
        LocalDate paidDate
) {
}
