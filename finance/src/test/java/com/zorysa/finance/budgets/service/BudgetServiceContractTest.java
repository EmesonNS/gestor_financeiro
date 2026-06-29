package com.zorysa.finance.budgets.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class BudgetServiceContractTest {

    @Test
    void shouldListBudgetsForAuthenticatedUserActiveInRequestedMonthWithPagination() {
        Method method = method("listBudgets");

        assertThat(method.getReturnType().getName())
                .as("listBudgets deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class);
        assertThat(integerParameterCount(method))
                .as("listBudgets deve receber month e year consultados")
                .isGreaterThanOrEqualTo(2);
        assertThat(method.getParameterCount())
                .as("listBudgets deve receber userId autenticado, month, year, categoryId e pageable")
                .isGreaterThanOrEqualTo(5);
    }

    @Test
    void shouldCreateBudgetForAuthenticatedUserOnly() {
        Method method = method("createBudget");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.budgets.dto.BudgetResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateBudgetRequest"));
    }

    @Test
    void shouldReadUpdateAndDeleteOnlyOwnedBudgets() {
        assertThat(method("getBudget")).satisfies(this::requiresAuthenticatedUserAndBudgetId);
        assertThat(method("updateBudget")).satisfies(this::requiresAuthenticatedUserAndBudgetId);
        assertThat(method("deleteBudget")).satisfies(this::requiresAuthenticatedUserAndBudgetId);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwnerActivePeriodCategoryAndOverlap() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.budgets.repository.BudgetRepository");

        assertThat(methodNames(repository)).contains(
                "findByIdAndUserId",
                "findAllActiveByUserId",
                "findOverlappingPeriodByUserIdAndCategoryId"
        );
        assertThat(parameterTypes(method(repository, "findAllActiveByUserId")))
                .as("consulta de orcamentos ativos deve aceitar Pageable")
                .contains(Pageable.class);
        assertThat(method(repository, "findOverlappingPeriodByUserIdAndCategoryId").getReturnType().getName())
                .as("consulta de conflito deve permitir detectar se existe sobreposicao antes de salvar")
                .isIn(Optional.class.getName(), Boolean.TYPE.getName(), Boolean.class.getName());
    }

    @Test
    void shouldBlockOverlappingBudgetPeriodsForSameOwnerAndCategory() {
        Class<?> service = findRequiredClass("com.zorysa.finance.budgets.service.BudgetService");

        assertThat(methodNames(service))
                .as("BudgetService deve validar conflito de periodo antes de criar ou editar")
                .contains("validateNoOverlappingPeriod");
        Method method = method(service, "validateNoOverlappingPeriod");

        assertThat(parameterTypes(method)).contains(UUID.class);
        assertThat(integerParameterCount(method))
                .as("validateNoOverlappingPeriod deve receber inicio e fim em mes/ano")
                .isGreaterThanOrEqualTo(4);
    }

    @Test
    void shouldCalculateSpentAmountFromPaidExpensesOwnedByAuthenticatedUserForRequestedMonth() {
        Class<?> service = findRequiredClass("com.zorysa.finance.budgets.service.BudgetService");

        assertThat(methodNames(service))
                .as("BudgetService deve calcular gasto realizado por categoria usando despesas pagas do usuario no mes consultado")
                .contains("calculateSpentAmount");
        Method method = method(service, "calculateSpentAmount");

        assertThat(method.getReturnType()).isEqualTo(BigDecimal.class);
        assertThat(parameterTypes(method)).contains(UUID.class);
        assertThat(integerParameterCount(method))
                .as("calculateSpentAmount deve receber month e year consultados")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUseOnlyExpenseCategoriesForBudgets() {
        Class<?> service = findRequiredClass("com.zorysa.finance.budgets.service.BudgetService");

        assertThat(Arrays.stream(service.getDeclaredFields()).map(field -> field.getType().getName()))
                .as("BudgetService deve validar categoria e calcular consumo com dominios existentes")
                .anySatisfy(typeName -> assertThat(typeName).contains("categories"));
    }

    private void requiresAuthenticatedUserAndBudgetId(Method method) {
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter dois UUIDs: userId e budgetId")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.budgets.service.BudgetService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.budgets.dto." + simpleName);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Orcamentos", exception);
        }
    }

    private Class<?>[] parameterTypes(Method method) {
        return method.getParameterTypes();
    }

    private String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(Method::getName)
                .toArray(String[]::new);
    }

    private long integerParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes())
                .filter(type -> type.equals(Integer.TYPE) || type.equals(Integer.class))
                .count();
    }
}
