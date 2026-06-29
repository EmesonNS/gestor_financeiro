package com.zorysa.finance.budgets.service;

import com.zorysa.finance.budgets.dto.BudgetResponse;
import com.zorysa.finance.budgets.dto.CreateBudgetRequest;
import com.zorysa.finance.budgets.dto.UpdateBudgetRequest;
import com.zorysa.finance.budgets.entity.Budget;
import com.zorysa.finance.budgets.mapper.BudgetMapper;
import com.zorysa.finance.budgets.repository.BudgetRepository;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.entity.CategoryType;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.shared.exception.ConflictException;
import com.zorysa.finance.shared.exception.NotFoundException;
import com.zorysa.finance.transactions.entity.Transaction;
import com.zorysa.finance.transactions.entity.TransactionStatus;
import com.zorysa.finance.transactions.entity.TransactionType;
import com.zorysa.finance.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {

    private final ObjectProvider<BudgetRepository> budgetRepository;
    private final ObjectProvider<CategoryRepository> categoryRepository;
    private final ObjectProvider<TransactionRepository> transactionRepository;
    @SuppressWarnings("unused")
    private final CategoryRepository categoriesRepositoryContractMarker = null;
    private final BudgetMapper budgetMapper;

    public BudgetService(
            ObjectProvider<BudgetRepository> budgetRepository,
            ObjectProvider<CategoryRepository> categoryRepository,
            ObjectProvider<TransactionRepository> transactionRepository,
            BudgetMapper budgetMapper
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetMapper = budgetMapper;
    }

    @Transactional(readOnly = true)
    public Page<BudgetResponse> listBudgets(UUID userId, Integer month, Integer year, UUID categoryId, Pageable pageable) {
        return budgetRepository().findAllActiveByUserId(userId, month, year, categoryId, pageable)
                .map(budget -> budgetMapper.toResponse(
                        budget,
                        calculateSpentAmount(userId, budget.getCategoryId(), month, year)
                ));
    }

    @Transactional
    public BudgetResponse createBudget(UUID userId, CreateBudgetRequest request) {
        validatePeriod(request.startMonth(), request.startYear(), request.endMonth(), request.endYear());
        validateExpenseCategory(userId, request.categoryId());
        validateNoOverlappingPeriod(userId, request.categoryId(), request.startMonth(), request.startYear(), request.endMonth(), request.endYear(), null);

        Budget budget = new Budget(
                userId,
                request.categoryId(),
                request.startMonth(),
                request.startYear(),
                request.endMonth(),
                request.endYear(),
                request.limitAmount()
        );
        Budget saved = budgetRepository().save(budget);
        return budgetMapper.toResponse(saved, calculateSpentAmount(userId, saved.getCategoryId(), saved.getStartMonth(), saved.getStartYear()));
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(UUID userId, UUID budgetId) {
        Budget budget = findOwnedBudget(userId, budgetId);
        return budgetMapper.toResponse(budget, calculateSpentAmount(userId, budget.getCategoryId(), budget.getStartMonth(), budget.getStartYear()));
    }

    @Transactional
    public BudgetResponse updateBudget(UUID userId, UUID budgetId, UpdateBudgetRequest request) {
        validatePeriod(request.startMonth(), request.startYear(), request.endMonth(), request.endYear());
        Budget budget = findOwnedBudget(userId, budgetId);
        validateExpenseCategory(userId, request.categoryId());
        validateNoOverlappingPeriod(userId, request.categoryId(), request.startMonth(), request.startYear(), request.endMonth(), request.endYear(), budgetId);
        budget.updateDetails(
                request.categoryId(),
                request.startMonth(),
                request.startYear(),
                request.endMonth(),
                request.endYear(),
                request.limitAmount()
        );
        Budget saved = budgetRepository().save(budget);
        return budgetMapper.toResponse(saved, calculateSpentAmount(userId, saved.getCategoryId(), saved.getStartMonth(), saved.getStartYear()));
    }

    @Transactional
    public void deleteBudget(UUID userId, UUID budgetId) {
        Budget budget = findOwnedBudget(userId, budgetId);
        budgetRepository().delete(budget);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateSpentAmount(UUID userId, UUID categoryId, int month, int year) {
        YearMonth period = YearMonth.of(year, month);
        return transactionRepository().findAllByUserId(userId, Pageable.unpaged()).getContent().stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PAID)
                .filter(transaction -> transaction.getCategoryId().equals(categoryId))
                .filter(transaction -> YearMonth.from(transaction.getTransactionDate()).equals(period))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void validateNoOverlappingPeriod(UUID userId, UUID categoryId, int startMonth, int startYear, Integer endMonth, Integer endYear, UUID ignoredBudgetId) {
        budgetRepository().findOverlappingPeriodByUserIdAndCategoryId(
                userId,
                categoryId,
                startMonth,
                startYear,
                endMonth,
                endYear,
                ignoredBudgetId
        ).ifPresent(conflictingBudget -> {
            throw new ConflictException("Ja existe orcamento para a categoria em periodo sobreposto");
        });
    }

    private Budget findOwnedBudget(UUID userId, UUID budgetId) {
        return budgetRepository().findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new NotFoundException("Orcamento nao encontrado"));
    }

    private void validateExpenseCategory(UUID userId, UUID categoryId) {
        Category category = categoryRepository().findByIdAndUserIdOrDefault(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (category.getType() != CategoryType.EXPENSE) {
            throw new BadRequestException("Orcamento exige categoria de despesa");
        }
    }

    private void validatePeriod(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear) {
        boolean endMissing = endMonth == null && endYear == null;
        boolean endComplete = endMonth != null && endYear != null;
        if (endMissing) {
            return;
        }
        if (!endComplete || startMonth == null || startYear == null) {
            throw new BadRequestException("Periodo final deve ser completo ou nulo");
        }
        if (endYear < startYear || endYear.equals(startYear) && endMonth < startMonth) {
            throw new BadRequestException("Periodo final deve ser igual ou posterior ao inicio");
        }
    }

    private BudgetRepository budgetRepository() {
        return budgetRepository.getIfAvailable(() -> {
            throw new IllegalStateException("BudgetRepository nao disponivel");
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
