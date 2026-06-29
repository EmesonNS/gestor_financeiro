package com.zorysa.finance.goals.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class GoalServiceContractTest {

    @Test
    void shouldListGoalsForAuthenticatedUserWithStatusFilterAndPagination() {
        Method method = method("listGoals");

        assertThat(method.getReturnType().getName())
                .as("listGoals deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class, statusClass());
        assertThat(method.getParameterCount())
                .as("listGoals deve receber userId autenticado, status e pageable")
                .isGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldCreateGoalForAuthenticatedUserOnly() {
        Method method = method("createGoal");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.goals.dto.GoalResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateGoalRequest"));
    }

    @Test
    void shouldReadUpdateDeleteAndUpdateProgressOnlyOwnedGoals() {
        assertThat(method("getGoal")).satisfies(this::requiresAuthenticatedUserAndGoalId);
        assertThat(method("updateGoal")).satisfies(this::requiresAuthenticatedUserAndGoalId);
        assertThat(method("deleteGoal")).satisfies(this::requiresAuthenticatedUserAndGoalId);
        assertThat(method("updateProgress")).satisfies(this::requiresAuthenticatedUserAndGoalId);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwnerAndStatus() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.goals.repository.GoalRepository");

        assertThat(methodNames(repository)).contains(
                "findByIdAndUserId",
                "findAllByUserId"
        );
        assertThat(parameterTypes(method(repository, "findAllByUserId")))
                .as("consulta de colecao de metas deve aceitar Pageable")
                .contains(Pageable.class);
    }

    @Test
    void shouldExposeProgressUpdateContract() {
        Method method = method("updateProgress");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.goals.dto.GoalResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("UpdateGoalProgressRequest"));
    }

    @Test
    void shouldCalculateCompletionPercentageAsBigDecimal() {
        Class<?> service = findRequiredClass("com.zorysa.finance.goals.service.GoalService");

        assertThat(methodNames(service))
                .as("GoalService deve expor calculo percentual para resposta de metas")
                .contains("completionPercentage");
        Method method = method(service, "completionPercentage");

        assertThat(method.getReturnType()).isEqualTo(BigDecimal.class);
        assertThat(parameterTypes(method)).contains(BigDecimal.class);
    }

    private void requiresAuthenticatedUserAndGoalId(Method method) {
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter dois UUIDs: userId e goalId")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.goals.service.GoalService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.goals.dto." + simpleName);
    }

    private Class<?> statusClass() {
        return findRequiredClass("com.zorysa.finance.goals.entity.GoalStatus");
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Metas financeiras", exception);
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
}
