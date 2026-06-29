package com.zorysa.finance.budgets.mapper;

import com.zorysa.finance.budgets.dto.BudgetResponse;
import com.zorysa.finance.budgets.entity.Budget;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public BudgetResponse toResponse(Budget budget, BigDecimal spentAmount) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategoryId(),
                budget.getStartMonth(),
                budget.getStartYear(),
                budget.getEndMonth(),
                budget.getEndYear(),
                budget.getLimitAmount(),
                spentAmount,
                budget.remainingAmount(spentAmount),
                budget.usagePercentage(spentAmount),
                budget.isExceeded(spentAmount),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
