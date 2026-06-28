package com.zorysa.finance.dashboard.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.dashboard.dto.DashboardMonthlyResponse;
import com.zorysa.finance.dashboard.dto.DashboardSummaryResponse;
import com.zorysa.finance.dashboard.dto.ExpenseByCategoryResponse;
import com.zorysa.finance.dashboard.dto.IncomeExpenseMonthlyResponse;
import com.zorysa.finance.dashboard.service.DashboardService;
import com.zorysa.finance.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public DashboardController(DashboardService dashboardService, CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.currentUser = currentUser;
    }

    @GetMapping("/summary")
    DashboardSummaryResponse getSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return dashboardService.getSummary(currentUser.id(), month, year);
    }

    @GetMapping("/monthly")
    DashboardMonthlyResponse getMonthlySummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return dashboardService.getMonthlySummary(currentUser.id(), month, year);
    }

    @GetMapping("/charts/expenses-by-category")
    PageResponse<ExpenseByCategoryResponse> getExpensesByCategory(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return PageResponse.from(dashboardService.getExpensesByCategory(currentUser.id(), month, year, pageable));
    }

    @GetMapping("/charts/income-vs-expense")
    PageResponse<IncomeExpenseMonthlyResponse> getIncomeVsExpense(
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return PageResponse.from(dashboardService.getIncomeVsExpense(currentUser.id(), year, pageable));
    }
}
