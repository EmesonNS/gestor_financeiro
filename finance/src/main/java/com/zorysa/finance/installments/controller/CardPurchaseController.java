package com.zorysa.finance.installments.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.installments.dto.CardPurchaseResponse;
import com.zorysa.finance.installments.dto.CreateCardPurchaseRequest;
import com.zorysa.finance.installments.dto.UpdateCardPurchaseRequest;
import com.zorysa.finance.installments.entity.PurchaseStatus;
import com.zorysa.finance.installments.service.InstallmentService;
import com.zorysa.finance.shared.dto.PageResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CardPurchaseController {

    private final InstallmentService installmentService;
    private final CurrentUser currentUser;

    public CardPurchaseController(InstallmentService installmentService, CurrentUser currentUser) {
        this.installmentService = installmentService;
        this.currentUser = currentUser;
    }

    @PostMapping("/credit-cards/{cardId}/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    CardPurchaseResponse createPurchase(
            @PathVariable UUID cardId,
            @Valid @RequestBody CreateCardPurchaseRequest request
    ) {
        return installmentService.createPurchase(currentUser.id(), cardId, request);
    }

    @GetMapping("/credit-cards/{cardId}/purchases")
    PageResponse<CardPurchaseResponse> listPurchases(
            @PathVariable UUID cardId,
            @RequestParam(required = false) PurchaseStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable
    ) {
        return PageResponse.from(installmentService.listPurchases(currentUser.id(), cardId, status, startDate, endDate, pageable));
    }

    @GetMapping("/card-purchases/{purchaseId}")
    CardPurchaseResponse getPurchase(@PathVariable UUID purchaseId) {
        return installmentService.getPurchase(currentUser.id(), purchaseId);
    }

    @PutMapping("/card-purchases/{purchaseId}")
    CardPurchaseResponse updatePurchase(
            @PathVariable UUID purchaseId,
            @Valid @RequestBody UpdateCardPurchaseRequest request
    ) {
        return installmentService.updatePurchase(currentUser.id(), purchaseId, request);
    }

    @DeleteMapping("/card-purchases/{purchaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePurchase(@PathVariable UUID purchaseId) {
        installmentService.deletePurchase(currentUser.id(), purchaseId);
    }
}
