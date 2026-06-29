package com.zorysa.finance.budgets.repository;

import com.zorysa.finance.budgets.entity.Budget;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select budget
            from Budget budget
            where budget.userId = :userId
              and (:categoryId is null or budget.categoryId = :categoryId)
              and (budget.startYear * 100 + budget.startMonth) <= (:year * 100 + :month)
              and (budget.endYear is null or (budget.endYear * 100 + budget.endMonth) >= (:year * 100 + :month))
            """)
    Page<Budget> findAllActiveByUserId(
            @Param("userId") UUID userId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

    @Query("""
            select budget
            from Budget budget
            where budget.userId = :userId
              and budget.categoryId = :categoryId
              and (:ignoredBudgetId is null or budget.id <> :ignoredBudgetId)
              and (budget.endYear is null or (budget.endYear * 100 + budget.endMonth) >= (:startYear * 100 + :startMonth))
              and (:endYear is null or (budget.startYear * 100 + budget.startMonth) <= (:endYear * 100 + :endMonth))
            """)
    Optional<Budget> findOverlappingPeriodByUserIdAndCategoryId(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startMonth") Integer startMonth,
            @Param("startYear") Integer startYear,
            @Param("endMonth") Integer endMonth,
            @Param("endYear") Integer endYear,
            @Param("ignoredBudgetId") UUID ignoredBudgetId
    );
}
