package com.zorysa.finance.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DashboardModuleContractTest {

    @Test
    void shouldExposeDashboardModuleClassesRequiredByDashboardFeature() {
        assertThatClassExists("com.zorysa.finance.dashboard.controller.DashboardController");
        assertThatClassExists("com.zorysa.finance.dashboard.service.DashboardService");
        assertThatClassExists("com.zorysa.finance.dashboard.dto.DashboardSummaryResponse");
        assertThatClassExists("com.zorysa.finance.dashboard.dto.DashboardMonthlyResponse");
        assertThatClassExists("com.zorysa.finance.dashboard.dto.ExpenseByCategoryResponse");
        assertThatClassExists("com.zorysa.finance.dashboard.dto.IncomeExpenseMonthlyResponse");
    }

    @Test
    void shouldMapDashboardControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.dashboard.controller.DashboardController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("DashboardController deve declarar base path /api/dashboard")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api/dashboard");

        assertThat(mappedEndpoints(controller)).contains(
                "GET /summary",
                "GET /monthly",
                "GET /charts/expenses-by-category",
                "GET /charts/income-vs-expense"
        );
    }

    @Test
    void shouldExposeDashboardServiceOperationsForAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.dashboard.service.DashboardService");

        assertThat(methodNames(service)).contains(
                "getSummary",
                "getMonthlySummary",
                "getExpensesByCategory",
                "getIncomeVsExpense"
        );
        assertThatMethod(method(service, "getSummary")).hasParameter(UUID.class);
        assertThatMethod(method(service, "getMonthlySummary")).hasParameter(UUID.class);
        assertThatMethod(method(service, "getExpensesByCategory")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getIncomeVsExpense")).hasParameter(UUID.class).hasParameter(Pageable.class);
    }

    @Test
    void shouldRequirePaginationOnlyOnDashboardCollectionEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.dashboard.controller.DashboardController");

        assertThat(Arrays.asList(mappedMethod(controller, "/summary").getParameterTypes()))
                .as("GET /api/dashboard/summary retorna objeto unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/monthly").getParameterTypes()))
                .as("GET /api/dashboard/monthly retorna objeto unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/charts/expenses-by-category").getParameterTypes()))
                .as("GET /api/dashboard/charts/expenses-by-category deve aceitar page, size e sort via Pageable")
                .contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(controller, "/charts/income-vs-expense").getParameterTypes()))
                .as("GET /api/dashboard/charts/income-vs-expense deve aceitar page, size e sort via Pageable")
                .contains(Pageable.class);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Dashboard", exception);
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
                .orElseThrow(() -> new AssertionError("DashboardController deve mapear GET " + path));
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
