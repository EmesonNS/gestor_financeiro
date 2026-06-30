package com.zorysa.finance.reports.dto;

import com.zorysa.finance.installments.entity.InstallmentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record FutureInstallmentReportResponse(
        UUID installmentId,
        UUID purchaseId,
        UUID cardId,
        String cardName,
        String description,
        int installmentNumber,
        int totalInstallments,
        BigDecimal amount,
        int competenceMonth,
        int competenceYear,
        InstallmentStatus status
) {
}
