package com.zorysa.finance.reports.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReportResponseContractTest {

    @Test
    void shouldExposeTransactionReportRow() {
        Class<?> response = requiredRecord("com.zorysa.finance.reports.dto.TransactionReportResponse");

        assertThat(recordComponentNames(response)).contains(
                "transactionId",
                "description",
                "type",
                "categoryId",
                "categoryName",
                "accountId",
                "accountName",
                "amount",
                "transactionDate",
                "status"
        );
        assertThat(componentType(response, "transactionId")).isEqualTo(UUID.class);
        assertThat(componentType(response, "amount")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "transactionDate")).isEqualTo(LocalDate.class);
    }

    @Test
    void shouldExposeExpenseByCategoryReportRow() {
        Class<?> response = requiredRecord("com.zorysa.finance.reports.dto.ExpenseByCategoryReportResponse");

        assertThat(recordComponentNames(response)).containsExactly(
                "categoryId",
                "categoryName",
                "totalAmount",
                "percentage"
        );
        assertThat(componentType(response, "categoryId")).isEqualTo(UUID.class);
        assertThat(componentType(response, "totalAmount")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "percentage")).isEqualTo(BigDecimal.class);
    }

    @Test
    void shouldExposeMonthlyEvolutionReportRow() {
        Class<?> response = requiredRecord("com.zorysa.finance.reports.dto.MonthlyEvolutionReportResponse");

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

    @Test
    void shouldExposeAccountBalanceReportRow() {
        Class<?> response = requiredRecord("com.zorysa.finance.reports.dto.AccountBalanceReportResponse");

        assertThat(recordComponentNames(response)).containsExactly(
                "accountId",
                "accountName",
                "accountType",
                "balance"
        );
        assertThat(componentType(response, "accountId")).isEqualTo(UUID.class);
        assertThat(componentType(response, "balance")).isEqualTo(BigDecimal.class);
    }

    @Test
    void shouldExposeBudgetVsActualReportRow() {
        Class<?> response = requiredRecord("com.zorysa.finance.reports.dto.BudgetVsActualReportResponse");

        assertThat(recordComponentNames(response)).containsExactly(
                "budgetId",
                "categoryId",
                "categoryName",
                "plannedAmount",
                "actualAmount",
                "remainingAmount",
                "percentageUsed",
                "exceeded"
        );
        assertThat(componentType(response, "plannedAmount")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "actualAmount")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "remainingAmount")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "exceeded")).isIn(Boolean.TYPE, Boolean.class);
    }

    @Test
    void shouldExposeCreditCardExpenseReportRow() {
        Class<?> response = requiredRecord("com.zorysa.finance.reports.dto.CreditCardExpenseReportResponse");

        assertThat(recordComponentNames(response)).containsExactly(
                "cardId",
                "cardName",
                "categoryId",
                "categoryName",
                "totalAmount"
        );
        assertThat(componentType(response, "cardId")).isEqualTo(UUID.class);
        assertThat(componentType(response, "totalAmount")).isEqualTo(BigDecimal.class);
    }

    @Test
    void shouldExposeFutureInstallmentReportRow() {
        Class<?> response = requiredRecord("com.zorysa.finance.reports.dto.FutureInstallmentReportResponse");

        assertThat(recordComponentNames(response)).contains(
                "installmentId",
                "purchaseId",
                "cardId",
                "cardName",
                "description",
                "installmentNumber",
                "totalInstallments",
                "amount",
                "competenceMonth",
                "competenceYear",
                "status"
        );
        assertThat(componentType(response, "installmentId")).isEqualTo(UUID.class);
        assertThat(componentType(response, "amount")).isEqualTo(BigDecimal.class);
        assertThat(componentType(response, "competenceMonth")).isIn(Integer.TYPE, Integer.class);
        assertThat(componentType(response, "competenceYear")).isIn(Integer.TYPE, Integer.class);
    }

    private Class<?> requiredRecord(String className) {
        try {
            Class<?> type = Class.forName(className);
            assertThat(type.isRecord()).as(className + " deve ser record").isTrue();
            return type;
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Relatorios", exception);
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
