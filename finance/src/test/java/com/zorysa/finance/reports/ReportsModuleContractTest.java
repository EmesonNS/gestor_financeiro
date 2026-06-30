package com.zorysa.finance.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class ReportsModuleContractTest {

    @Test
    void shouldExposeReportsModuleClassesRequiredByReportsFeature() {
        assertThatClassExists("com.zorysa.finance.reports.controller.ReportController");
        assertThatClassExists("com.zorysa.finance.reports.service.ReportService");
        assertThatClassExists("com.zorysa.finance.reports.dto.TransactionReportResponse");
        assertThatClassExists("com.zorysa.finance.reports.dto.ExpenseByCategoryReportResponse");
        assertThatClassExists("com.zorysa.finance.reports.dto.MonthlyEvolutionReportResponse");
        assertThatClassExists("com.zorysa.finance.reports.dto.AccountBalanceReportResponse");
        assertThatClassExists("com.zorysa.finance.reports.dto.BudgetVsActualReportResponse");
        assertThatClassExists("com.zorysa.finance.reports.dto.CreditCardExpenseReportResponse");
        assertThatClassExists("com.zorysa.finance.reports.dto.FutureInstallmentReportResponse");
    }

    @Test
    void shouldMapReportControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.reports.controller.ReportController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("ReportController deve declarar base path /api/reports")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api/reports");

        assertThat(mappedEndpoints(controller)).contains(
                "GET /transactions",
                "GET /expenses-by-category",
                "GET /monthly-evolution",
                "GET /accounts-balance",
                "GET /budget-vs-actual",
                "GET /credit-card-expenses",
                "GET /future-installments"
        );
    }

    @Test
    void shouldRequirePaginationOnEveryReportEndpoint() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.reports.controller.ReportController");

        assertThat(Arrays.asList(mappedMethod(controller, "/transactions").getParameterTypes())).contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/expenses-by-category").getParameterTypes())).contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/monthly-evolution").getParameterTypes())).contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/accounts-balance").getParameterTypes())).contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/budget-vs-actual").getParameterTypes())).contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/credit-card-expenses").getParameterTypes())).contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/future-installments").getParameterTypes())).contains(Pageable.class);
    }

    @Test
    void shouldExposeReportServiceOperationsForAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.reports.service.ReportService");

        assertThat(methodNames(service)).contains(
                "getTransactionsReport",
                "getExpensesByCategoryReport",
                "getMonthlyEvolutionReport",
                "getAccountsBalanceReport",
                "getBudgetVsActualReport",
                "getCreditCardExpensesReport",
                "getFutureInstallmentsReport"
        );
        assertThatMethod(method(service, "getTransactionsReport")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getExpensesByCategoryReport")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getMonthlyEvolutionReport")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getAccountsBalanceReport")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getBudgetVsActualReport")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getCreditCardExpensesReport")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getFutureInstallmentsReport")).hasParameter(UUID.class).hasParameter(Pageable.class);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Relatorios", exception);
        }
    }

    private Method method(Class<?> service, String methodName) {
        return Arrays.stream(service.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(service.getSimpleName() + " deve expor metodo " + methodName));
    }

    private String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).map(Method::getName).toArray(String[]::new);
    }

    private String[] mappedEndpoints(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .flatMap(method -> {
                    GetMapping get = method.getAnnotation(GetMapping.class);
                    if (get != null) return Arrays.stream(paths(get.path(), get.value())).map(path -> "GET " + path);
                    return Arrays.<String>stream(new String[0]);
                })
                .toArray(String[]::new);
    }

    private String[] paths(String[] path, String[] value) {
        String[] mappings = path.length > 0 ? path : value;
        return mappings.length == 0 ? new String[]{""} : mappings;
    }

    private Method mappedMethod(Class<?> controller, String path) {
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(GetMapping.class))
                .filter(method -> Arrays.asList(paths(
                        method.getAnnotation(GetMapping.class).path(),
                        method.getAnnotation(GetMapping.class).value()
                )).contains(path))
                .findFirst()
                .orElseThrow(() -> new AssertionError("ReportController deve mapear GET " + path));
    }

    private MethodContract assertThatMethod(Method method) {
        return new MethodContract(method);
    }

    private static class MethodContract {
        private final Method method;

        MethodContract(Method method) {
            this.method = method;
        }

        MethodContract hasParameter(Class<?> type) {
            assertThat(Arrays.asList(method.getParameterTypes()))
                    .as(method.getName() + " deve receber parametro " + type.getSimpleName())
                    .contains(type);
            return this;
        }
    }
}
