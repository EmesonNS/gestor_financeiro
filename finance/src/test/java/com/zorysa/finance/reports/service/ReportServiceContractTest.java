package com.zorysa.finance.reports.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class ReportServiceContractTest {

    @Test
    void shouldGenerateTransactionsReportForAuthenticatedUserAndFilters() {
        Method method = method("getTransactionsReport");

        assertPagedReport(method);
        assertThat(parameterTypes(method)).contains(UUID.class, LocalDate.class, transactionTypeClass(), Pageable.class);
        assertThat(uuidParameterCount(method))
                .as("getTransactionsReport deve receber userId, categoryId e accountId")
                .isGreaterThanOrEqualTo(3);
        assertThat(localDateParameterCount(method))
                .as("getTransactionsReport deve receber startDate e endDate")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGenerateExpensesByCategoryReportForAuthenticatedUserAndPeriod() {
        Method method = method("getExpensesByCategoryReport");

        assertPagedReport(method);
        assertThat(parameterTypes(method)).contains(UUID.class, LocalDate.class, Pageable.class);
        assertThat(localDateParameterCount(method))
                .as("getExpensesByCategoryReport deve receber startDate e endDate")
                .isEqualTo(2);
    }

    @Test
    void shouldGenerateMonthlyEvolutionReportForAuthenticatedUserAndYear() {
        Method method = method("getMonthlyEvolutionReport");

        assertPagedReport(method);
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class);
        assertThat(integerParameterCount(method))
                .as("getMonthlyEvolutionReport deve receber year")
                .isEqualTo(1);
    }

    @Test
    void shouldGenerateAccountsBalanceReportForAuthenticatedUserAndDate() {
        Method method = method("getAccountsBalanceReport");

        assertPagedReport(method);
        assertThat(parameterTypes(method)).contains(UUID.class, LocalDate.class, Pageable.class);
    }

    @Test
    void shouldGenerateBudgetVsActualReportForAuthenticatedUserMonthAndYear() {
        Method method = method("getBudgetVsActualReport");

        assertPagedReport(method);
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class);
        assertThat(integerParameterCount(method))
                .as("getBudgetVsActualReport deve receber month e year")
                .isEqualTo(2);
    }

    @Test
    void shouldGenerateCreditCardExpensesReportForAuthenticatedUserCardAndPeriod() {
        Method method = method("getCreditCardExpensesReport");

        assertPagedReport(method);
        assertThat(parameterTypes(method)).contains(UUID.class, LocalDate.class, Pageable.class);
        assertThat(uuidParameterCount(method))
                .as("getCreditCardExpensesReport deve receber userId e cardId")
                .isGreaterThanOrEqualTo(2);
        assertThat(localDateParameterCount(method))
                .as("getCreditCardExpensesReport deve receber startDate e endDate")
                .isEqualTo(2);
    }

    @Test
    void shouldGenerateFutureInstallmentsReportForAuthenticatedUserCardAndStartCompetence() {
        Method method = method("getFutureInstallmentsReport");

        assertPagedReport(method);
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class);
        assertThat(uuidParameterCount(method))
                .as("getFutureInstallmentsReport deve receber userId e cardId")
                .isGreaterThanOrEqualTo(2);
        assertThat(integerParameterCount(method))
                .as("getFutureInstallmentsReport deve receber fromMonth e fromYear")
                .isEqualTo(2);
    }

    @Test
    void shouldNotPersistReportState() {
        Class<?> service = findRequiredClass("com.zorysa.finance.reports.service.ReportService");

        assertThat(Arrays.stream(service.getDeclaredFields()).map(field -> field.getType().getName()))
                .as("ReportService deve consultar services/projections existentes e nao persistir estado proprio")
                .allSatisfy(typeName -> assertThat(typeName).doesNotContain("reports.repository"));
    }

    private void assertPagedReport(Method method) {
        assertThat(method.getReturnType().getName())
                .as(method.getName() + " deve retornar uma pagina de linhas de relatorio")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(Pageable.class);
        assertThat(parameterTypes(method)).contains(UUID.class);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.reports.service.ReportService");
        return Arrays.stream(service.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("ReportService deve expor metodo " + methodName));
    }

    private Class<?> transactionTypeClass() {
        return findRequiredClass("com.zorysa.finance.transactions.entity.TransactionType");
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Relatorios", exception);
        }
    }

    private Class<?>[] parameterTypes(Method method) {
        return method.getParameterTypes();
    }

    private long uuidParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count();
    }

    private long localDateParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes()).filter(LocalDate.class::equals).count();
    }

    private long integerParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes())
                .filter(type -> type.equals(Integer.TYPE) || type.equals(Integer.class))
                .count();
    }
}
