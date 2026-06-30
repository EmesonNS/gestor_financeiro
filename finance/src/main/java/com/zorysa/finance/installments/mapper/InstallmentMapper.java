package com.zorysa.finance.installments.mapper;

import com.zorysa.finance.installments.dto.CardPurchaseResponse;
import com.zorysa.finance.installments.dto.InstallmentResponse;
import com.zorysa.finance.installments.entity.CardPurchase;
import com.zorysa.finance.installments.entity.CreditCardInstallment;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InstallmentMapper {

    public CardPurchaseResponse toPurchaseResponse(CardPurchase purchase, List<InstallmentResponse> installments) {
        return new CardPurchaseResponse(
                purchase.getId(),
                purchase.getCreditCardId(),
                purchase.getCategoryId(),
                purchase.getDescription(),
                purchase.getTotalAmount(),
                purchase.getPurchaseDate(),
                purchase.getInstallmentCount(),
                purchase.getStatus(),
                purchase.getNotes(),
                installments,
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }

    public InstallmentResponse toInstallmentResponse(CreditCardInstallment installment) {
        return new InstallmentResponse(
                installment.getId(),
                installment.getPurchaseId(),
                installment.getInvoiceId(),
                installment.getInstallmentNumber(),
                installment.getTotalInstallments(),
                installment.getAmount(),
                installment.getCompetenceMonth(),
                installment.getCompetenceYear(),
                installment.getStatus(),
                installment.getCreatedAt(),
                installment.getUpdatedAt()
        );
    }
}
