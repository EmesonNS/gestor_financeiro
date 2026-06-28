package com.zorysa.finance.transactions.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class TransactionServiceContractTest {

    @Test
    void shouldListTransactionsForAuthenticatedUserWithDocumentedFiltersAndPagination() {
        Method method = method("listTransactions");

        assertThat(method.getReturnType().getName())
                .as("listTransactions deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class, LocalDate.class);
        assertThat(method.getParameterCount())
                .as("listTransactions deve receber userId autenticado, filtros documentados e pageable")
                .isGreaterThanOrEqualTo(8);
    }

    @Test
    void shouldCreateTransactionForAuthenticatedUserOnly() {
        Method method = method("createTransaction");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.transactions.dto.TransactionResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateTransactionRequest"));
    }

    @Test
    void shouldReadUpdateDeletePayAndCancelOnlyOwnedTransactions() {
        assertThat(method("getTransaction")).satisfies(this::requiresAuthenticatedUserAndTransactionId);
        assertThat(method("updateTransaction")).satisfies(this::requiresAuthenticatedUserAndTransactionId);
        assertThat(method("deleteTransaction")).satisfies(this::requiresAuthenticatedUserAndTransactionId);
        assertThat(method("markAsPaid")).satisfies(this::requiresAuthenticatedUserAndTransactionId);
        assertThat(method("cancelTransaction")).satisfies(this::requiresAuthenticatedUserAndTransactionId);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwner() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.transactions.repository.TransactionRepository");

        assertThat(methodNames(repository)).contains(
                "findByIdAndUserId",
                "findAllByUserId"
        );
        assertThat(parameterTypes(method(repository, "findAllByUserId")))
                .as("consulta de colecao de transacoes deve aceitar Pageable")
                .contains(Pageable.class);
    }

    private void requiresAuthenticatedUserAndTransactionId(Method method) {
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter dois UUIDs: userId e transactionId")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.transactions.service.TransactionService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.transactions.dto." + simpleName);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Transacoes", exception);
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
