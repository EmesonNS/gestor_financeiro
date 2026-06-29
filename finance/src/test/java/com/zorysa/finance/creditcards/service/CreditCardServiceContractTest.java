package com.zorysa.finance.creditcards.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class CreditCardServiceContractTest {

    @Test
    void shouldListCreditCardsForAuthenticatedUserWithArchivedFilterAndPagination() {
        Method method = method("listCreditCards");

        assertThat(method.getReturnType().getName())
                .as("listCreditCards deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class, Boolean.class);
        assertThat(method.getParameterCount())
                .as("listCreditCards deve receber userId autenticado, archived e pageable")
                .isGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldCreateCreditCardForAuthenticatedUserOnly() {
        Method method = method("createCreditCard");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.creditcards.dto.CreditCardResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateCreditCardRequest"));
    }

    @Test
    void shouldReadUpdateDeleteAndArchiveOnlyOwnedCreditCards() {
        assertThat(method("getCreditCard")).satisfies(this::requiresAuthenticatedUserAndCardId);
        assertThat(method("updateCreditCard")).satisfies(this::requiresAuthenticatedUserAndCardId);
        assertThat(method("deleteCreditCard")).satisfies(this::requiresAuthenticatedUserAndCardId);
        assertThat(method("archiveCreditCard")).satisfies(this::requiresAuthenticatedUserAndCardId);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwnerAndArchived() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.creditcards.repository.CreditCardRepository");

        assertThat(methodNames(repository)).contains(
                "findByIdAndUserId",
                "findAllByUserId",
                "findAllByUserIdAndArchived"
        );
        assertThat(parameterTypes(method(repository, "findAllByUserId")))
                .as("consulta de colecao de cartoes deve aceitar Pageable")
                .contains(Pageable.class);
    }

    @Test
    void shouldCalculateUsedLimitForCardOwnedByAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.creditcards.service.CreditCardService");

        assertThat(methodNames(service))
                .as("CreditCardService deve expor calculo de limite usado para montar a resposta")
                .contains("calculateUsedLimit");
        Method method = method(service, "calculateUsedLimit");

        assertThat(method.getReturnType()).isEqualTo(BigDecimal.class);
        assertThat(parameterTypes(method)).contains(UUID.class);
    }

    @Test
    void shouldCalculateAvailableLimitFromTotalAndUsedLimit() {
        Class<?> service = findRequiredClass("com.zorysa.finance.creditcards.service.CreditCardService");

        assertThat(methodNames(service))
                .as("CreditCardService deve expor calculo de limite disponivel")
                .contains("calculateAvailableLimit");
        Method method = method(service, "calculateAvailableLimit");

        assertThat(method.getReturnType()).isEqualTo(BigDecimal.class);
        assertThat(parameterTypes(method)).contains(BigDecimal.class);
    }

    @Test
    void shouldArchiveInsteadOfAllowingFuturePurchasesOnArchivedCards() {
        Class<?> service = findRequiredClass("com.zorysa.finance.creditcards.service.CreditCardService");

        assertThat(methodNames(service))
                .as("CreditCardService deve expor validacao reutilizavel para bloquear compras futuras em cartao arquivado")
                .contains("validateCanReceivePurchases");
    }

    private void requiresAuthenticatedUserAndCardId(Method method) {
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter dois UUIDs: userId e creditCardId")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.creditcards.service.CreditCardService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.creditcards.dto." + simpleName);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Cartoes de credito", exception);
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
