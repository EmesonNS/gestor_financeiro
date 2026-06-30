package com.zorysa.finance.invoices.mapper;

import com.zorysa.finance.invoices.dto.InvoiceResponse;
import com.zorysa.finance.invoices.entity.CreditCardInvoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(CreditCardInvoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCreditCardId(),
                invoice.getReferenceMonth(),
                invoice.getReferenceYear(),
                invoice.getClosingDate(),
                invoice.getDueDate(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getPaidAt(),
                invoice.getPaymentAccountId(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
