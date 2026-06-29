package com.zorysa.finance.creditcards.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.creditcards.dto.CreateCreditCardRequest;
import com.zorysa.finance.creditcards.dto.CreditCardResponse;
import com.zorysa.finance.creditcards.dto.UpdateCreditCardRequest;
import com.zorysa.finance.creditcards.service.CreditCardService;
import com.zorysa.finance.shared.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credit-cards")
public class CreditCardController {

    private final CreditCardService creditCardService;
    private final CurrentUser currentUser;

    public CreditCardController(CreditCardService creditCardService, CurrentUser currentUser) {
        this.creditCardService = creditCardService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<CreditCardResponse> listCreditCards(
            @RequestParam(required = false) Boolean archived,
            Pageable pageable
    ) {
        return PageResponse.from(creditCardService.listCreditCards(currentUser.id(), archived, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreditCardResponse createCreditCard(@Valid @RequestBody CreateCreditCardRequest request) {
        return creditCardService.createCreditCard(currentUser.id(), request);
    }

    @GetMapping("/{id}")
    CreditCardResponse getCreditCard(@PathVariable UUID id) {
        return creditCardService.getCreditCard(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    CreditCardResponse updateCreditCard(@PathVariable UUID id, @Valid @RequestBody UpdateCreditCardRequest request) {
        return creditCardService.updateCreditCard(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCreditCard(@PathVariable UUID id) {
        creditCardService.deleteCreditCard(currentUser.id(), id);
    }

    @PatchMapping("/{id}/archive")
    CreditCardResponse archiveCreditCard(@PathVariable UUID id) {
        return creditCardService.archiveCreditCard(currentUser.id(), id);
    }
}
