package com.zorysa.finance.dashboard.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DashboardResponseContractTest {

    @Test
    void shouldExposeDashboardSummaryMoneyFields() {
        Class<?> response = findRequiredClass("com.zorysa.finance.dashboard.dto.DashboardSummaryResponse");

        assertThat(recordComponentNames(response)).containsExactly(
                "totalBalance",
                "monthlyIncome",
                "monthlyExpense",
                "monthlyBalance",
                "expectedBalance",
                "openInvoicesTotal",
                "currentInvoiceAmount",
                "creditLimitUsed"
        );
        assertThat(response.getRecordComponents())
                .allSatisfy(component -> assertThat(component.getType()).isEqualTo(BigDecimal.class));
    }

    @Test
    void shouldExposeMonthlyDashboardResponseForCurrentPeriod() {
        Class<?> response = findRequiredClass("com.zorysa.finance.dashboard.dto.DashboardMonthlyResponse");

        assertThat(recordComponentNames(response)).contains(
                "month",
                "year",
                "income",
                "expense",
                "balance",
                "expectedBalance"
        );
    }

    @Test
    void shouldExposeExpenseByCategoryChartRow() {
        Class<?> response = findRequiredClass("com.zorysa.finance.dashboard.dto.ExpenseByCategoryResponse");

        assertThat(recordComponentNames(response)).containsExactly(
                "categoryId",
                "categoryName",
                "amount"
        );
        assertThat(componentType(response, "categoryId")).isEqualTo(UUID.class);
        assertThat(componentType(response, "categoryName")).isEqualTo(String.class);
        assertThat(componentType(response, "amount")).isEqualTo(BigDecimal.class);
    }

    @Test
    void shouldExposeIncomeVsExpenseMonthlyChartRow() {
        Class<?> response = findRequiredClass("com.zorysa.finance.dashboard.dto.IncomeExpenseMonthlyResponse");

        assertThat(recordComponentNames(response)).containsExactly(
                "month",
                "income",
                "expense",
                "balance"
        );
        assertThat(componentType(response, "month")).isIn(Integer.TYPE, Integer.class);
        assertThat(componentType(response, "income")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "expense")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "balance")).isEqualTo(BigDecimal.class);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            Class<?> type = Class.forName(className);
            assertThat(type.isRecord()).as(className + " deve ser record").isTrue();
            return type;
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Dashboard", exception);
        }
    }

    private String[] recordComponentNames(Class<?> type) {
        return java.util.Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getName)
                .toArray(String[]::new);
    }

    private Class<?> componentType(Class<?> type, String componentName) {
        return java.util.Arrays.stream(type.getRecordComponents())
                .filter(component -> component.getName().equals(componentName))
                .map(RecordComponent::getType)
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor componente " + componentName));
    }
}
