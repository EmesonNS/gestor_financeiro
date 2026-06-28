package com.zorysa.finance.transactions.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.shared.dto.PageResponse;
import com.zorysa.finance.transactions.dto.CreateTransactionRequest;
import com.zorysa.finance.transactions.dto.MarkTransactionAsPaidRequest;
import com.zorysa.finance.transactions.dto.TransactionResponse;
import com.zorysa.finance.transactions.dto.UpdateTransactionRequest;
import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import com.zorysa.finance.transactions.service.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUser currentUser;

    public TransactionController(TransactionService transactionService, CurrentUser currentUser) {
        this.transactionService = transactionService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<TransactionResponse> listTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID accountId,
            Pageable pageable
    ) {
        return PageResponse.from(transactionService.listTransactions(
                currentUser.id(),
                startDate,
                endDate,
                type,
                status,
                categoryId,
                accountId,
                pageable
        ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(currentUser.id(), request);
    }

    @GetMapping("/{id}")
    TransactionResponse getTransaction(@PathVariable UUID id) {
        return transactionService.getTransaction(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    TransactionResponse updateTransaction(@PathVariable UUID id, @Valid @RequestBody UpdateTransactionRequest request) {
        return transactionService.updateTransaction(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(currentUser.id(), id);
    }

    @PatchMapping("/{id}/mark-as-paid")
    TransactionResponse markAsPaid(
            @PathVariable UUID id,
            @RequestBody(required = false) MarkTransactionAsPaidRequest request
    ) {
        return transactionService.markAsPaid(currentUser.id(), id, request);
    }

    @PatchMapping("/{id}/cancel")
    TransactionResponse cancelTransaction(@PathVariable UUID id) {
        return transactionService.cancelTransaction(currentUser.id(), id);
    }
}
