package com.zorysa.finance.reports.service;

import com.zorysa.finance.accounts.entity.Account;
import com.zorysa.finance.budgets.entity.Budget;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.creditcards.entity.CreditCard;
import com.zorysa.finance.installments.entity.CardPurchase;
import com.zorysa.finance.installments.entity.CreditCardInstallment;
import com.zorysa.finance.installments.entity.InstallmentStatus;
import com.zorysa.finance.reports.dto.AccountBalanceReportResponse;
import com.zorysa.finance.reports.dto.BudgetVsActualReportResponse;
import com.zorysa.finance.reports.dto.CreditCardExpenseReportResponse;
import com.zorysa.finance.reports.dto.ExpenseByCategoryReportResponse;
import com.zorysa.finance.reports.dto.FutureInstallmentReportResponse;
import com.zorysa.finance.reports.dto.MonthlyEvolutionReportResponse;
import com.zorysa.finance.reports.dto.TransactionReportResponse;
import com.zorysa.finance.transactions.entity.Transaction;
import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ObjectProvider<EntityManager> entityManager;

    public ReportService(ObjectProvider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Page<TransactionReportResponse> getTransactionsReport(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            UUID categoryId,
            UUID accountId,
            Pageable pageable
    ) {
        List<Transaction> transactions = entityManager().createQuery("""
                        select transaction
                        from Transaction transaction
                        where transaction.userId = :userId
                        order by transaction.transactionDate desc, transaction.createdAt desc
                        """, Transaction.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .filter(transaction -> startDate == null || !transaction.getTransactionDate().isBefore(startDate))
                .filter(transaction -> endDate == null || !transaction.getTransactionDate().isAfter(endDate))
                .filter(transaction -> type == null || transaction.getType() == type)
                .filter(transaction -> categoryId == null || categoryId.equals(transaction.getCategoryId()))
                .filter(transaction -> accountId == null || accountId.equals(transaction.getAccountId()))
                .toList();

        List<TransactionReportResponse> rows = transactions.stream()
                .map(transaction -> new TransactionReportResponse(
                        transaction.getId(),
                        transaction.getDescription(),
                        transaction.getType(),
                        transaction.getCategoryId(),
                        categoryName(transaction.getCategoryId()),
                        transaction.getAccountId(),
                        accountName(transaction.getAccountId()),
                        transaction.getAmount(),
                        transaction.getTransactionDate(),
                        transaction.getStatus()
                ))
                .toList();
        return page(rows, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseByCategoryReportResponse> getExpensesByCategoryReport(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        List<Transaction> expenses = paidExpenses(userId, startDate, endDate);
        BigDecimal total = expenses.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ExpenseByCategoryReportResponse> rows = expenses.stream()
                .collect(java.util.stream.Collectors.groupingBy(Transaction::getCategoryId))
                .entrySet()
                .stream()
                .map(entry -> {
                    BigDecimal categoryTotal = entry.getValue().stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal percentage = total.signum() == 0
                            ? BigDecimal.ZERO
                            : categoryTotal.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
                    return new ExpenseByCategoryReportResponse(entry.getKey(), categoryName(entry.getKey()), categoryTotal, percentage);
                })
                .sorted((left, right) -> right.totalAmount().compareTo(left.totalAmount()))
                .toList();
        return page(rows, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MonthlyEvolutionReportResponse> getMonthlyEvolutionReport(UUID userId, Integer year, Pageable pageable) {
        int targetYear = year == null ? LocalDate.now().getYear() : year;
        List<Transaction> transactions = entityManager().createQuery("""
                        select transaction
                        from Transaction transaction
                        where transaction.userId = :userId
                        """, Transaction.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .filter(transaction -> transaction.getTransactionDate().getYear() == targetYear)
                .toList();

        List<MonthlyEvolutionReportResponse> rows = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            int currentMonth = month;
            BigDecimal income = transactions.stream()
                    .filter(transaction -> transaction.getTransactionDate().getMonthValue() == currentMonth)
                    .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                    .filter(transaction -> transaction.getStatus() == TransactionStatus.RECEIVED)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expense = transactions.stream()
                    .filter(transaction -> transaction.getTransactionDate().getMonthValue() == currentMonth)
                    .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                    .filter(transaction -> transaction.getStatus() == TransactionStatus.PAID)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(new MonthlyEvolutionReportResponse(month, income, expense, income.subtract(expense)));
        }
        return page(rows, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AccountBalanceReportResponse> getAccountsBalanceReport(UUID userId, LocalDate date, Pageable pageable) {
        List<AccountBalanceReportResponse> rows = entityManager().createQuery("""
                        select account
                        from Account account
                        where account.userId = :userId
                        order by account.name asc
                        """, Account.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(account -> new AccountBalanceReportResponse(
                        account.getId(),
                        account.getName(),
                        account.getType(),
                        account.getCurrentBalance()
                ))
                .toList();
        return page(rows, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BudgetVsActualReportResponse> getBudgetVsActualReport(UUID userId, Integer month, Integer year, Pageable pageable) {
        int targetMonth = month == null ? LocalDate.now().getMonthValue() : month;
        int targetYear = year == null ? LocalDate.now().getYear() : year;
        LocalDate start = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<BudgetVsActualReportResponse> rows = entityManager().createQuery("""
                        select budget
                        from Budget budget
                        where budget.userId = :userId
                        """, Budget.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .filter(budget -> budget.isActiveIn(targetMonth, targetYear))
                .map(budget -> {
                    BigDecimal actual = paidExpenses(userId, start, end).stream()
                            .filter(transaction -> transaction.getCategoryId().equals(budget.getCategoryId()))
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal remaining = budget.remainingAmount(actual);
                    BigDecimal percentage = budget.getLimitAmount().signum() == 0
                            ? BigDecimal.ZERO
                            : budget.usagePercentage(actual);
                    return new BudgetVsActualReportResponse(
                            budget.getId(),
                            budget.getCategoryId(),
                            categoryName(budget.getCategoryId()),
                            budget.getLimitAmount(),
                            actual,
                            remaining,
                            percentage,
                            budget.isExceeded(actual)
                    );
                })
                .toList();
        return page(rows, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CreditCardExpenseReportResponse> getCreditCardExpensesReport(
            UUID userId,
            UUID cardId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        List<CardPurchase> purchases = entityManager().createQuery("""
                        select purchase
                        from CardPurchase purchase
                        where purchase.userId = :userId
                        """, CardPurchase.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .filter(purchase -> cardId == null || cardId.equals(purchase.getCreditCardId()))
                .filter(purchase -> startDate == null || !purchase.getPurchaseDate().isBefore(startDate))
                .filter(purchase -> endDate == null || !purchase.getPurchaseDate().isAfter(endDate))
                .toList();

        List<CreditCardExpenseReportResponse> rows = purchases.stream()
                .collect(java.util.stream.Collectors.groupingBy(purchase -> purchase.getCreditCardId() + ":" + purchase.getCategoryId()))
                .values()
                .stream()
                .map(group -> {
                    CardPurchase first = group.getFirst();
                    BigDecimal total = group.stream().map(CardPurchase::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CreditCardExpenseReportResponse(
                            first.getCreditCardId(),
                            cardName(first.getCreditCardId()),
                            first.getCategoryId(),
                            categoryName(first.getCategoryId()),
                            total
                    );
                })
                .toList();
        return page(rows, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FutureInstallmentReportResponse> getFutureInstallmentsReport(
            UUID userId,
            UUID cardId,
            Integer fromMonth,
            Integer fromYear,
            Integer toMonth,
            Integer toYear,
            Pageable pageable
    ) {
        YearMonth from = fromMonth != null && fromYear != null ? YearMonth.of(fromYear, fromMonth) : YearMonth.now();
        YearMonth to = toMonth != null && toYear != null ? YearMonth.of(toYear, toMonth) : null;
        List<FutureInstallmentReportResponse> rows = entityManager().createQuery("""
                        select installment
                        from CreditCardInstallment installment
                        where installment.userId = :userId
                        order by installment.competenceYear asc, installment.competenceMonth asc, installment.installmentNumber asc
                        """, CreditCardInstallment.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .filter(installment -> installment.getStatus() != InstallmentStatus.CANCELED)
                .filter(installment -> {
                    YearMonth competence = YearMonth.of(installment.getCompetenceYear(), installment.getCompetenceMonth());
                    return competence.compareTo(from) >= 0 && (to == null || competence.compareTo(to) <= 0);
                })
                .map(installment -> {
                    CardPurchase purchase = entityManager().find(CardPurchase.class, installment.getPurchaseId());
                    if (purchase == null || (cardId != null && !cardId.equals(purchase.getCreditCardId()))) {
                        return null;
                    }
                    return new FutureInstallmentReportResponse(
                            installment.getId(),
                            installment.getPurchaseId(),
                            purchase.getCreditCardId(),
                            cardName(purchase.getCreditCardId()),
                            purchase.getDescription(),
                            installment.getInstallmentNumber(),
                            installment.getTotalInstallments(),
                            installment.getAmount(),
                            installment.getCompetenceMonth(),
                            installment.getCompetenceYear(),
                            installment.getStatus()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return page(rows, pageable);
    }

    private List<Transaction> paidExpenses(UUID userId, LocalDate startDate, LocalDate endDate) {
        return entityManager().createQuery("""
                        select transaction
                        from Transaction transaction
                        where transaction.userId = :userId
                          and transaction.type = :type
                          and transaction.status = :status
                        """, Transaction.class)
                .setParameter("userId", userId)
                .setParameter("type", TransactionType.EXPENSE)
                .setParameter("status", TransactionStatus.PAID)
                .getResultList()
                .stream()
                .filter(transaction -> startDate == null || !transaction.getTransactionDate().isBefore(startDate))
                .filter(transaction -> endDate == null || !transaction.getTransactionDate().isAfter(endDate))
                .toList();
    }

    private String categoryName(UUID categoryId) {
        Category category = categoryId == null ? null : entityManager().find(Category.class, categoryId);
        return category == null ? null : category.getName();
    }

    private String accountName(UUID accountId) {
        Account account = accountId == null ? null : entityManager().find(Account.class, accountId);
        return account == null ? null : account.getName();
    }

    private String cardName(UUID cardId) {
        CreditCard card = cardId == null ? null : entityManager().find(CreditCard.class, cardId);
        return card == null ? null : card.getName();
    }

    private <T> Page<T> page(List<T> rows, Pageable pageable) {
        int start = Math.toIntExact(Math.min(pageable.getOffset(), rows.size()));
        int end = Math.min(start + pageable.getPageSize(), rows.size());
        return new PageImpl<>(rows.subList(start, end), pageable, rows.size());
    }

    private EntityManager entityManager() {
        return entityManager.getIfAvailable(() -> {
            throw new IllegalStateException("EntityManager nao disponivel");
        });
    }
}
