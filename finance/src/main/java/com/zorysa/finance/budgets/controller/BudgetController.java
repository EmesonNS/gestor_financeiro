package com.zorysa.finance.budgets.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.budgets.dto.BudgetResponse;
import com.zorysa.finance.budgets.dto.CreateBudgetRequest;
import com.zorysa.finance.budgets.dto.UpdateBudgetRequest;
import com.zorysa.finance.budgets.service.BudgetService;
import com.zorysa.finance.shared.dto.PageResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final CurrentUser currentUser;

    public BudgetController(BudgetService budgetService, CurrentUser currentUser) {
        this.budgetService = budgetService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<BudgetResponse> listBudgets(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) UUID categoryId,
            Pageable pageable
    ) {
        return PageResponse.from(budgetService.listBudgets(currentUser.id(), month, year, categoryId, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BudgetResponse createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        return budgetService.createBudget(currentUser.id(), request);
    }

    @GetMapping("/{id}")
    BudgetResponse getBudget(@PathVariable UUID id) {
        return budgetService.getBudget(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    BudgetResponse updateBudget(@PathVariable UUID id, @Valid @RequestBody UpdateBudgetRequest request) {
        return budgetService.updateBudget(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBudget(@PathVariable UUID id) {
        budgetService.deleteBudget(currentUser.id(), id);
    }
}
