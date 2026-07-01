package com.zorysa.finance.reports.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.reports.dto.AccountBalanceReportResponse;
import com.zorysa.finance.reports.dto.BudgetVsActualReportResponse;
import com.zorysa.finance.reports.dto.CreditCardExpenseReportResponse;
import com.zorysa.finance.reports.dto.ExpenseByCategoryReportResponse;
import com.zorysa.finance.reports.dto.FutureInstallmentReportResponse;
import com.zorysa.finance.reports.dto.MonthlyEvolutionReportResponse;
import com.zorysa.finance.reports.dto.TransactionReportResponse;
import com.zorysa.finance.reports.service.ReportService;
import com.zorysa.finance.shared.dto.PageResponse;
import com.zorysa.finance.transactions.entity.TransactionType;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final CurrentUser currentUser;

    public ReportController(ReportService reportService, CurrentUser currentUser) {
        this.reportService = reportService;
        this.currentUser = currentUser;
    }

    @GetMapping("/transactions")
    PageResponse<TransactionReportResponse> getTransactionsReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID accountId,
            Pageable pageable
    ) {
        return PageResponse.from(reportService.getTransactionsReport(currentUser.id(), startDate, endDate, type, categoryId, accountId, pageable));
    }

    @GetMapping("/expenses-by-category")
    PageResponse<ExpenseByCategoryReportResponse> getExpensesByCategoryReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable
    ) {
        return PageResponse.from(reportService.getExpensesByCategoryReport(currentUser.id(), startDate, endDate, pageable));
    }

    @GetMapping("/monthly-evolution")
    PageResponse<MonthlyEvolutionReportResponse> getMonthlyEvolutionReport(
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return PageResponse.from(reportService.getMonthlyEvolutionReport(currentUser.id(), year, pageable));
    }

    @GetMapping("/accounts-balance")
    PageResponse<AccountBalanceReportResponse> getAccountsBalanceReport(
            @RequestParam(required = false) LocalDate date,
            Pageable pageable
    ) {
        return PageResponse.from(reportService.getAccountsBalanceReport(currentUser.id(), date, pageable));
    }

    @GetMapping("/budget-vs-actual")
    PageResponse<BudgetVsActualReportResponse> getBudgetVsActualReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return PageResponse.from(reportService.getBudgetVsActualReport(currentUser.id(), month, year, pageable));
    }

    @GetMapping("/credit-card-expenses")
    PageResponse<CreditCardExpenseReportResponse> getCreditCardExpensesReport(
            @RequestParam(required = false) UUID cardId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable
    ) {
        return PageResponse.from(reportService.getCreditCardExpensesReport(currentUser.id(), cardId, startDate, endDate, pageable));
    }

    @GetMapping("/future-installments")
    PageResponse<FutureInstallmentReportResponse> getFutureInstallmentsReport(
            @RequestParam(required = false) UUID cardId,
            @RequestParam(required = false) Integer fromMonth,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toMonth,
            @RequestParam(required = false) Integer toYear,
            Pageable pageable
    ) {
        return PageResponse.from(reportService.getFutureInstallmentsReport(
                currentUser.id(),
                cardId,
                fromMonth,
                fromYear,
                toMonth,
                toYear,
                pageable
        ));
    }
}
