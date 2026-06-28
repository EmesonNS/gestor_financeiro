package com.zorysa.finance.accounts.controller;

import com.zorysa.finance.accounts.dto.AccountResponse;
import com.zorysa.finance.accounts.dto.CreateAccountRequest;
import com.zorysa.finance.accounts.dto.UpdateAccountRequest;
import com.zorysa.finance.accounts.entity.AccountType;
import com.zorysa.finance.accounts.service.AccountService;
import com.zorysa.finance.auth.security.CurrentUser;
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
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final CurrentUser currentUser;

    public AccountController(AccountService accountService, CurrentUser currentUser) {
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<AccountResponse> listAccounts(
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) AccountType type,
            Pageable pageable
    ) {
        return PageResponse.from(accountService.listAccounts(currentUser.id(), archived, type, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(currentUser.id(), request);
    }

    @GetMapping("/{id}")
    AccountResponse getAccount(@PathVariable UUID id) {
        return accountService.getAccount(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    AccountResponse updateAccount(@PathVariable UUID id, @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateAccount(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(currentUser.id(), id);
    }

    @PatchMapping("/{id}/archive")
    AccountResponse archiveAccount(@PathVariable UUID id) {
        return accountService.archiveAccount(currentUser.id(), id);
    }
}
