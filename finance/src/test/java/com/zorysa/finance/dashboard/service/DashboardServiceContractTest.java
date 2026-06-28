package com.zorysa.finance.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class DashboardServiceContractTest {

    @Test
    void shouldCalculateSummaryForAuthenticatedUserAndRequestedPeriod() {
        Method method = method("getSummary");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.dashboard.dto.DashboardSummaryResponse");
        assertThat(parameterTypes(method)).contains(UUID.class);
        assertThat(integerParameterCount(method)).isEqualTo(2);
        assertThat(method.getParameterCount())
                .as("getSummary deve receber userId autenticado, month e year")
                .isEqualTo(3);
    }

    @Test
    void shouldCalculateMonthlyDashboardForAuthenticatedUserAndRequestedPeriod() {
        Method method = method("getMonthlySummary");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.dashboard.dto.DashboardMonthlyResponse");
        assertThat(parameterTypes(method)).contains(UUID.class);
        assertThat(integerParameterCount(method)).isEqualTo(2);
        assertThat(method.getParameterCount())
                .as("getMonthlySummary deve receber userId autenticado, month e year")
                .isEqualTo(3);
    }

    @Test
    void shouldPageExpensesByCategoryForAuthenticatedUserAndRequestedPeriod() {
        Method method = method("getExpensesByCategory");

        assertThat(method.getReturnType().getName())
                .as("getExpensesByCategory deve retornar resposta paginada")
                .contains("Page");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class);
        assertThat(integerParameterCount(method)).isEqualTo(2);
        assertThat(method.getParameterCount())
                .as("getExpensesByCategory deve receber userId autenticado, month, year e pageable")
                .isEqualTo(4);
    }

    @Test
    void shouldPageIncomeVsExpenseSeriesForAuthenticatedUserAndRequestedYear() {
        Method method = method("getIncomeVsExpense");

        assertThat(method.getReturnType().getName())
                .as("getIncomeVsExpense deve retornar resposta paginada")
                .contains("Page");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class);
        assertThat(integerParameterCount(method)).isEqualTo(1);
        assertThat(method.getParameterCount())
                .as("getIncomeVsExpense deve receber userId autenticado, year e pageable")
                .isEqualTo(3);
    }

    @Test
    void shouldDependOnlyOnReadModelsOrExistingDomainServices() {
        Class<?> service = findRequiredClass("com.zorysa.finance.dashboard.service.DashboardService");

        assertThat(Arrays.stream(service.getDeclaredFields()).map(field -> field.getType().getName()))
                .as("DashboardService deve consultar dominios existentes e nao persistir estado proprio")
                .allSatisfy(typeName -> assertThat(typeName).doesNotContain("dashboard.repository"));
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.dashboard.service.DashboardService");
        return Arrays.stream(service.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("DashboardService deve expor metodo " + methodName));
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Dashboard", exception);
        }
    }

    private Class<?>[] parameterTypes(Method method) {
        return method.getParameterTypes();
    }
    private long integerParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes())
                .filter(type -> type.equals(Integer.TYPE) || type.equals(Integer.class))
                .count();
    }
}
