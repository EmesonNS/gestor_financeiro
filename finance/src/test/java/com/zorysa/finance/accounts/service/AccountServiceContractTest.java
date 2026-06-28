package com.zorysa.finance.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class AccountServiceContractTest {

    @Test
    void shouldListAccountsForAuthenticatedUserWithDocumentedFilters() {
        Method method = method("listAccounts");

        assertThat(method.getReturnType().getName())
                .as("listAccounts deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class);
        assertThat(method.getParameterCount())
                .as("listAccounts deve receber userId autenticado, archived, type e pageable")
                .isGreaterThanOrEqualTo(4);
    }

    @Test
    void shouldCreateAccountForAuthenticatedUserOnly() {
        Method method = method("createAccount");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.accounts.dto.AccountResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateAccountRequest"));
    }

    @Test
    void shouldReadAndMutateOnlyAccountsOwnedByAuthenticatedUser() {
        assertThat(method("getAccount")).satisfies(this::requiresAuthenticatedUserAndAccountId);
        assertThat(method("updateAccount")).satisfies(this::requiresAuthenticatedUserAndAccountId);
        assertThat(method("archiveAccount")).satisfies(this::requiresAuthenticatedUserAndAccountId);
        assertThat(method("deleteAccount")).satisfies(this::requiresAuthenticatedUserAndAccountId);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwner() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.accounts.repository.AccountRepository");

        assertThat(methodNames(repository)).contains(
                "findByIdAndUserId",
                "findAllByUserId"
        );
    }

    private void requiresAuthenticatedUserAndAccountId(Method method) {
        assertThat(parameterTypes(method))
                .as(method.getName() + " deve receber userId autenticado e accountId")
                .contains(UUID.class);
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter dois UUIDs: userId e accountId")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.accounts.service.AccountService");
        return Arrays.stream(service.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("AccountService deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.accounts.dto." + simpleName);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Contas financeiras", exception);
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
