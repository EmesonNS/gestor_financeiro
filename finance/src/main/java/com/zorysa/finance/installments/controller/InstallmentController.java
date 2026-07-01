package com.zorysa.finance.installments.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.installments.dto.InstallmentResponse;
import com.zorysa.finance.installments.entity.InstallmentStatus;
import com.zorysa.finance.installments.service.InstallmentService;
import com.zorysa.finance.shared.dto.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InstallmentController {

    private final InstallmentService installmentService;
    private final CurrentUser currentUser;

    public InstallmentController(InstallmentService installmentService, CurrentUser currentUser) {
        this.installmentService = installmentService;
        this.currentUser = currentUser;
    }

    @GetMapping("/installments")
    PageResponse<InstallmentResponse> listInstallments(
            @RequestParam(required = false) InstallmentStatus status,
            @RequestParam(required = false) UUID cardId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return PageResponse.from(installmentService.listInstallments(currentUser.id(), status, cardId, month, year, pageable));
    }

    @GetMapping("/installments/future")
    PageResponse<InstallmentResponse> listFutureInstallments(
            @RequestParam(required = false) UUID cardId,
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear,
            Pageable pageable
    ) {
        return PageResponse.from(installmentService.listFutureInstallments(
                currentUser.id(),
                cardId,
                fromMonth,
                fromYear,
                toMonth,
                toYear,
                pageable
        ));
    }

    @GetMapping("/card-purchases/{purchaseId}/installments")
    PageResponse<InstallmentResponse> listPurchaseInstallments(@PathVariable UUID purchaseId, Pageable pageable) {
        return PageResponse.from(installmentService.listPurchaseInstallments(currentUser.id(), purchaseId, pageable));
    }
}
