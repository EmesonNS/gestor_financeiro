package com.zorysa.finance.invoices.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.invoices.dto.InvoiceResponse;
import com.zorysa.finance.invoices.dto.PayInvoiceRequest;
import com.zorysa.finance.invoices.entity.InvoiceStatus;
import com.zorysa.finance.invoices.service.InvoiceService;
import com.zorysa.finance.shared.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final CurrentUser currentUser;

    public InvoiceController(InvoiceService invoiceService, CurrentUser currentUser) {
        this.invoiceService = invoiceService;
        this.currentUser = currentUser;
    }

    @GetMapping("/credit-cards/{cardId}/invoices")
    PageResponse<InvoiceResponse> listInvoices(
            @PathVariable UUID cardId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return PageResponse.from(invoiceService.listInvoices(currentUser.id(), cardId, status, year, pageable));
    }

    @GetMapping("/credit-cards/{cardId}/invoices/current")
    InvoiceResponse getCurrentInvoice(@PathVariable UUID cardId) {
        return invoiceService.getCurrentInvoice(currentUser.id(), cardId);
    }

    @GetMapping("/invoices/{invoiceId}")
    InvoiceResponse getInvoice(@PathVariable UUID invoiceId) {
        return invoiceService.getInvoice(currentUser.id(), invoiceId);
    }

    @PatchMapping("/invoices/{invoiceId}/pay")
    InvoiceResponse payInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody PayInvoiceRequest request) {
        return invoiceService.payInvoice(currentUser.id(), invoiceId, request);
    }
}
