package com.zorysa.finance.dashboard.service;

import com.zorysa.finance.accounts.entity.Account;
import com.zorysa.finance.accounts.repository.AccountRepository;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.dashboard.dto.DashboardMonthlyResponse;
import com.zorysa.finance.dashboard.dto.DashboardSummaryResponse;
import com.zorysa.finance.dashboard.dto.ExpenseByCategoryResponse;
import com.zorysa.finance.dashboard.dto.IncomeExpenseMonthlyResponse;
import com.zorysa.finance.transactions.entity.Transaction;
import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import com.zorysa.finance.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ObjectProvider<AccountRepository> accountRepository;
    private final ObjectProvider<CategoryRepository> categoryRepository;
    private final ObjectProvider<TransactionRepository> transactionRepository;

    public DashboardService(
            ObjectProvider<AccountRepository> accountRepository,
            ObjectProvider<CategoryRepository> categoryRepository,
            ObjectProvider<TransactionRepository> transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(UUID userId, Integer month, Integer year) {
        YearMonth period = periodOrCurrent(month, year);
        List<Account> accounts = accounts(userId);
        List<Transaction> transactions = transactions(userId);

        BigDecimal totalBalance = accounts.stream()
                .filter(account -> !account.isArchived())
                .map(Account::getCurrentBalance)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal monthlyIncome = monthlyIncome(transactions, period);
        BigDecimal monthlyExpense = monthlyExpense(transactions, period);
        BigDecimal expectedBalance = expectedBalance(totalBalance, transactions, period);

        return new DashboardSummaryResponse(
                totalBalance,
                monthlyIncome,
                monthlyExpense,
                monthlyIncome.subtract(monthlyExpense),
                expectedBalance,
                ZERO,
                ZERO,
                ZERO
        );
    }

    @Transactional(readOnly = true)
    public DashboardMonthlyResponse getMonthlySummary(UUID userId, Integer month, Integer year) {
        YearMonth period = periodOrCurrent(month, year);
        List<Account> accounts = accounts(userId);
        List<Transaction> transactions = transactions(userId);
        BigDecimal totalBalance = accounts.stream()
                .filter(account -> !account.isArchived())
                .map(Account::getCurrentBalance)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal income = monthlyIncome(transactions, period);
        BigDecimal expense = monthlyExpense(transactions, period);

        return new DashboardMonthlyResponse(
                period.getMonthValue(),
                period.getYear(),
                income,
                expense,
                income.subtract(expense),
                expectedBalance(totalBalance, transactions, period)
        );
    }

    @Transactional(readOnly = true)
    public Page<ExpenseByCategoryResponse> getExpensesByCategory(
            UUID userId,
            Integer month,
            Integer year,
            Pageable pageable
    ) {
        YearMonth period = periodOrCurrent(month, year);
        Map<UUID, String> categoryNames = categories(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (first, second) -> first));
        Map<UUID, BigDecimal> totals = new HashMap<>();

        for (Transaction transaction : transactions(userId)) {
            if (transaction.getType() == TransactionType.EXPENSE
                    && transaction.getStatus() == TransactionStatus.PAID
                    && isInPeriod(transaction, period)) {
                totals.merge(transaction.getCategoryId(), transaction.getAmount(), BigDecimal::add);
            }
        }

        List<ExpenseByCategoryResponse> rows = totals.entrySet().stream()
                .map(entry -> new ExpenseByCategoryResponse(
                        entry.getKey(),
                        categoryNames.getOrDefault(entry.getKey(), "Categoria removida"),
                        entry.getValue()
                ))
                .sorted(expenseComparator(pageable.getSort()))
                .toList();
        return page(rows, pageable);
    }

    @Transactional(readOnly = true)
    public Page<IncomeExpenseMonthlyResponse> getIncomeVsExpense(UUID userId, Integer year, Pageable pageable) {
        int selectedYear = year == null ? LocalDate.now().getYear() : year;
        List<Transaction> transactions = transactions(userId);
        List<IncomeExpenseMonthlyResponse> rows = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            YearMonth period = YearMonth.of(selectedYear, month);
            BigDecimal income = monthlyIncome(transactions, period);
            BigDecimal expense = monthlyExpense(transactions, period);
            rows.add(new IncomeExpenseMonthlyResponse(month, income, expense, income.subtract(expense)));
        }

        rows.sort(incomeExpenseComparator(pageable.getSort()));
        return page(rows, pageable);
    }

    private YearMonth periodOrCurrent(Integer month, Integer year) {
        YearMonth current = YearMonth.now();
        int selectedMonth = month == null ? current.getMonthValue() : month;
        int selectedYear = year == null ? current.getYear() : year;
        return YearMonth.of(selectedYear, selectedMonth);
    }

    private BigDecimal monthlyIncome(List<Transaction> transactions, YearMonth period) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .filter(transaction -> transaction.getStatus() == TransactionStatus.RECEIVED)
                .filter(transaction -> isInPeriod(transaction, period))
                .map(Transaction::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal monthlyExpense(List<Transaction> transactions, YearMonth period) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PAID)
                .filter(transaction -> isInPeriod(transaction, period))
                .map(Transaction::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal expectedBalance(BigDecimal totalBalance, List<Transaction> transactions, YearMonth period) {
        BigDecimal pendingIncome = pendingTotal(transactions, period, TransactionType.INCOME);
        BigDecimal pendingExpense = pendingTotal(transactions, period, TransactionType.EXPENSE);
        return totalBalance.add(pendingIncome).subtract(pendingExpense);
    }

    private BigDecimal pendingTotal(List<Transaction> transactions, YearMonth period, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PENDING)
                .filter(transaction -> isInPeriod(transaction, period))
                .map(Transaction::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private boolean isInPeriod(Transaction transaction, YearMonth period) {
        return YearMonth.from(transaction.getTransactionDate()).equals(period);
    }

    private List<Account> accounts(UUID userId) {
        return accountRepository().findAllByUserId(userId, Pageable.unpaged()).getContent();
    }

    private List<Category> categories(UUID userId) {
        return categoryRepository().findAllByUserIdOrDefault(userId, Pageable.unpaged()).getContent();
    }

    private List<Transaction> transactions(UUID userId) {
        return transactionRepository().findAllByUserId(userId, Pageable.unpaged()).getContent();
    }

    private Comparator<ExpenseByCategoryResponse> expenseComparator(Sort sort) {
        return comparator(sort, Map.of(
                "categoryName", ExpenseByCategoryResponse::categoryName,
                "amount", ExpenseByCategoryResponse::amount
        ), Comparator.comparing(ExpenseByCategoryResponse::amount).reversed());
    }

    private Comparator<IncomeExpenseMonthlyResponse> incomeExpenseComparator(Sort sort) {
        return comparator(sort, Map.of(
                "month", IncomeExpenseMonthlyResponse::month,
                "income", IncomeExpenseMonthlyResponse::income,
                "expense", IncomeExpenseMonthlyResponse::expense,
                "balance", IncomeExpenseMonthlyResponse::balance
        ), Comparator.comparing(IncomeExpenseMonthlyResponse::month));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Comparator<T> comparator(Sort sort, Map<String, Function<T, ? extends Comparable>> accessors, Comparator<T> fallback) {
        if (sort.isUnsorted()) {
            return fallback;
        }
        Comparator<T> comparator = null;
        for (Sort.Order order : sort) {
            Function<T, ? extends Comparable> accessor = accessors.get(order.getProperty());
            if (accessor == null) {
                continue;
            }
            Comparator<T> next = Comparator.comparing(accessor, Comparator.nullsLast(Comparator.naturalOrder()));
            if (order.isDescending()) {
                next = next.reversed();
            }
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }
        return comparator == null ? fallback : comparator;
    }

    private <T> Page<T> page(List<T> rows, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(rows);
        }
        int start = Math.toIntExact(Math.min(pageable.getOffset(), rows.size()));
        int end = Math.min(start + pageable.getPageSize(), rows.size());
        return new PageImpl<>(rows.subList(start, end), pageable, rows.size());
    }

    private AccountRepository accountRepository() {
        return accountRepository.getIfAvailable(() -> {
            throw new IllegalStateException("AccountRepository nao disponivel");
        });
    }

    private CategoryRepository categoryRepository() {
        return categoryRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CategoryRepository nao disponivel");
        });
    }

    private TransactionRepository transactionRepository() {
        return transactionRepository.getIfAvailable(() -> {
            throw new IllegalStateException("TransactionRepository nao disponivel");
        });
    }
}
