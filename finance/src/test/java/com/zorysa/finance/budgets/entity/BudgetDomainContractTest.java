package com.zorysa.finance.budgets.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BudgetDomainContractTest {

    private static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID CATEGORY_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID OTHER_USER_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    @Test
    void shouldCreateBudgetForCategoryPeriodAndLimit() {
        Object budget = budget(6, 2026, 12, 2026, new BigDecimal("1000.00"));

        assertThat(value(budget, "getUserId")).isEqualTo(USER_ID);
        assertThat(value(budget, "getCategoryId")).isEqualTo(CATEGORY_ID);
        assertThat(value(budget, "getStartMonth")).isEqualTo(6);
        assertThat(value(budget, "getStartYear")).isEqualTo(2026);
        assertThat(value(budget, "getEndMonth")).isEqualTo(12);
        assertThat(value(budget, "getEndYear")).isEqualTo(2026);
        assertThat(money(budget, "getLimitAmount")).isEqualByComparingTo("1000.00");
    }

    @Test
    void shouldSupportOpenEndedBudget() {
        Object budget = budget(6, 2026, null, null, new BigDecimal("1000.00"));

        assertThat(booleanValue(budget, "hasOpenEndedPeriod")).isTrue();
        assertThat(booleanValue(budget, "isActiveIn", 1, 2035)).isTrue();
    }

    @Test
    void shouldIndicateWhetherBudgetIsActiveInRequestedMonth() {
        Object budget = budget(6, 2026, 12, 2026, new BigDecimal("1000.00"));

        assertThat(booleanValue(budget, "isActiveIn", 5, 2026)).isFalse();
        assertThat(booleanValue(budget, "isActiveIn", 6, 2026)).isTrue();
        assertThat(booleanValue(budget, "isActiveIn", 12, 2026)).isTrue();
        assertThat(booleanValue(budget, "isActiveIn", 1, 2027)).isFalse();
    }

    @Test
    void shouldDetectOverlappingPeriodsForSameCategory() {
        Object budget = budget(6, 2026, 12, 2026, new BigDecimal("1000.00"));

        assertThat(booleanValue(budget, "overlaps", 1, 2026, 5, 2026)).isFalse();
        assertThat(booleanValue(budget, "overlaps", 6, 2026, 6, 2026)).isTrue();
        assertThat(booleanValue(budget, "overlaps", 10, 2026, 2, 2027)).isTrue();
        assertThat(booleanValue(budget, "overlaps", 1, 2027, null, null)).isFalse();
    }

    @Test
    void shouldTreatOpenEndedBudgetAsOverlappingAnyFuturePeriod() {
        Object budget = budget(6, 2026, null, null, new BigDecimal("1000.00"));

        assertThat(booleanValue(budget, "overlaps", 1, 2026, 5, 2026)).isFalse();
        assertThat(booleanValue(budget, "overlaps", 6, 2026, 12, 2026)).isTrue();
        assertThat(booleanValue(budget, "overlaps", 1, 2035, null, null)).isTrue();
    }

    @Test
    void shouldCalculateRemainingAmountAndUsagePercentageFromPaidExpenses() {
        Object budget = budget(6, 2026, 12, 2026, new BigDecimal("1000.00"));

        assertThat(money(budget, "remainingAmount", new BigDecimal("250.50")))
                .isEqualByComparingTo("749.50");
        assertThat(money(budget, "usagePercentage", new BigDecimal("250.00")))
                .isEqualByComparingTo("25.00");
    }

    @Test
    void shouldIndicateExceededWhenSpentAmountIsGreaterThanLimit() {
        Object budget = budget(6, 2026, 12, 2026, new BigDecimal("1000.00"));

        assertThat(booleanValue(budget, "isExceeded", new BigDecimal("1000.01"))).isTrue();
        assertThat(booleanValue(budget, "isExceeded", new BigDecimal("1000.00"))).isFalse();
    }

    @Test
    void shouldBelongOnlyToOwner() {
        Object budget = budget(6, 2026, 12, 2026, new BigDecimal("1000.00"));

        assertThat(booleanValue(budget, "belongsTo", USER_ID)).isTrue();
        assertThat(booleanValue(budget, "belongsTo", OTHER_USER_ID)).isFalse();
    }

    @Test
    void shouldUpdateCategoryPeriodAndLimit() {
        Object budget = budget(6, 2026, 12, 2026, new BigDecimal("1000.00"));
        UUID newCategoryId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

        invoke(budget, "updateDetails", newCategoryId, 1, 2027, null, null, new BigDecimal("1200.00"));

        assertThat(value(budget, "getCategoryId")).isEqualTo(newCategoryId);
        assertThat(value(budget, "getStartMonth")).isEqualTo(1);
        assertThat(value(budget, "getStartYear")).isEqualTo(2027);
        assertThat(value(budget, "getEndMonth")).isNull();
        assertThat(value(budget, "getEndYear")).isNull();
        assertThat(money(budget, "getLimitAmount")).isEqualByComparingTo("1200.00");
    }

    private Object budget(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear, BigDecimal limitAmount) {
        try {
            Class<?> budgetClass = Class.forName("com.zorysa.finance.budgets.entity.Budget");
            Constructor<?> constructor = budgetClass.getConstructor(
                    UUID.class,
                    UUID.class,
                    Integer.TYPE,
                    Integer.TYPE,
                    Integer.class,
                    Integer.class,
                    BigDecimal.class
            );
            return constructor.newInstance(USER_ID, CATEGORY_ID, startMonth, startYear, endMonth, endYear, limitAmount);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Budget deve expor construtor com userId, categoryId, startMonth, startYear, endMonth, endYear e limitAmount", exception);
        }
    }

    private Object value(Object target, String methodName) {
        return invoke(target, methodName);
    }

    private BigDecimal money(Object target, String methodName, Object... args) {
        return (BigDecimal) invoke(target, methodName, args);
    }

    private boolean booleanValue(Object target, String methodName, Object... args) {
        return (boolean) invoke(target, methodName, args);
    }

    private Object invoke(Object target, String methodName, Object... args) {
        try {
            Method method = method(target.getClass(), methodName, args);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(target.getClass().getSimpleName() + " deve expor metodo " + methodName, exception);
        }
    }

    private Method method(Class<?> type, String methodName, Object[] args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int index = 0; index < args.length; index++) {
            parameterTypes[index] = args[index] instanceof Integer ? Integer.TYPE : args[index] == null ? Integer.class : args[index].getClass();
        }
        try {
            return type.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName, exception);
        }
    }
}
