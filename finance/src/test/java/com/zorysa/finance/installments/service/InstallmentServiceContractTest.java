package com.zorysa.finance.installments.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class InstallmentServiceContractTest {

    @Test
    void shouldCreatePurchaseForOwnedCardAndGenerateInstallments() {
        Method method = method("createPurchase");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.installments.dto.CardPurchaseResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateCardPurchaseRequest"));
        assertThat(uuidParameterCount(method))
                .as("createPurchase deve receber userId autenticado e creditCardId")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldListPurchasesForOwnedCardWithFiltersAndPagination() {
        Method method = method("listPurchases");

        assertThat(method.getReturnType().getName())
                .as("listPurchases deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class, statusClass("PurchaseStatus"));
        assertThat(parameterTypes(method))
                .as("listPurchases deve aceitar filtros startDate e endDate")
                .contains(LocalDate.class);
        assertThat(uuidParameterCount(method))
                .as("listPurchases deve receber userId e creditCardId")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldReadUpdateAndDeletePurchaseOnlyForAuthenticatedOwner() {
        assertThat(method("getPurchase")).satisfies(this::requiresAuthenticatedUserAndResourceId);
        assertThat(method("updatePurchase")).satisfies(method -> {
            requiresAuthenticatedUserAndResourceId(method);
            assertThat(parameterTypes(method)).contains(requestClass("UpdateCardPurchaseRequest"));
        });
        assertThat(method("deletePurchase")).satisfies(this::requiresAuthenticatedUserAndResourceId);
    }

    @Test
    void shouldListInstallmentsWithDocumentedFiltersAndPagination() {
        Method listInstallments = method("listInstallments");
        Method listFutureInstallments = method("listFutureInstallments");
        Method listPurchaseInstallments = method("listPurchaseInstallments");

        assertPagedResponse(listInstallments);
        assertPagedResponse(listFutureInstallments);
        assertPagedResponse(listPurchaseInstallments);
        assertThat(parameterTypes(listInstallments)).contains(UUID.class, Pageable.class, statusClass("InstallmentStatus"));
        assertThat(parameterTypes(listInstallments))
                .as("listInstallments deve aceitar filtros cardId, month e year")
                .contains(UUID.class, Integer.class);
        assertThat(parameterTypes(listFutureInstallments))
                .as("listFutureInstallments deve aceitar cardId, fromMonth, fromYear, toMonth e toYear")
                .contains(UUID.class, Integer.class);
        assertThat(integerParameterCount(listFutureInstallments))
                .as("listFutureInstallments deve receber fromMonth, fromYear, toMonth e toYear")
                .isGreaterThanOrEqualTo(4);
        assertThat(uuidParameterCount(listPurchaseInstallments))
                .as("listPurchaseInstallments deve receber userId e purchaseId")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldValidateFutureInstallmentsRangeBeforePagination() {
        Class<?> service = findRequiredClass("com.zorysa.finance.installments.service.InstallmentService");

        assertThat(methodNames(service))
                .as("InstallmentService deve validar range de competencias antes de consultar pagina")
                .contains("validateInstallmentCompetenceRange");
        Method method = method(service, "validateInstallmentCompetenceRange");

        assertThat(method.getReturnType()).isEqualTo(Void.TYPE);
        assertThat(integerParameterCount(method))
                .as("validateInstallmentCompetenceRange deve receber fromMonth, fromYear, toMonth e toYear")
                .isEqualTo(4);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwner() {
        Class<?> purchaseRepository = findRequiredClass("com.zorysa.finance.installments.repository.CardPurchaseRepository");
        Class<?> installmentRepository = findRequiredClass("com.zorysa.finance.installments.repository.CreditCardInstallmentRepository");

        assertThat(methodNames(purchaseRepository)).contains(
                "findByIdAndUserId",
                "findAllByUserIdAndCreditCardId"
        );
        assertThat(parameterTypes(method(purchaseRepository, "findAllByUserIdAndCreditCardId")))
                .as("consulta de colecao de compras deve aceitar Pageable")
                .contains(Pageable.class);

        assertThat(methodNames(installmentRepository)).contains(
                "findAllByUserId",
                "findAllByUserIdAndPurchaseId",
                "existsPaidInvoiceInstallmentByPurchaseId"
        );
        assertThat(parameterTypes(method(installmentRepository, "findAllByUserId")))
                .as("consulta de colecao de parcelas deve aceitar Pageable")
                .contains(Pageable.class);
    }

    @Test
    void shouldGenerateInstallmentsWithRoundingAdjustedInLastInstallment() {
        Method method = method("generateInstallments");

        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(parameterTypes(method)).contains(BigDecimal.class, LocalDate.class);
        assertThat(integerParameterCount(method))
                .as("generateInstallments deve receber quantidade de parcelas")
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldValidatePurchaseCanBeChangedBeforeEditOrCancel() {
        Method method = method("validatePurchaseCanBeChanged");

        assertThat(method.getReturnType()).isEqualTo(Void.TYPE);
        assertThat(parameterTypes(method)).contains(UUID.class);
    }

    @Test
    void shouldDependOnCreditCardInvoiceAndCategoryContracts() {
        Class<?> service = findRequiredClass("com.zorysa.finance.installments.service.InstallmentService");

        assertThat(fieldTypeNames(service))
                .as("InstallmentService deve validar cartao/categoria e associar parcelas a faturas")
                .anySatisfy(typeName -> assertThat(typeName).contains("creditcards"))
                .anySatisfy(typeName -> assertThat(typeName).contains("categories"))
                .anySatisfy(typeName -> assertThat(typeName).contains("invoices"));
    }

    private void assertPagedResponse(Method method) {
        assertThat(method.getReturnType().getName())
                .as(method.getName() + " deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(Pageable.class);
    }

    private void requiresAuthenticatedUserAndResourceId(Method method) {
        assertThat(uuidParameterCount(method))
                .as(method.getName() + " deve ter pelo menos userId e id do recurso")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.installments.service.InstallmentService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.installments.dto." + simpleName);
    }

    private Class<?> statusClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.installments.entity." + simpleName);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Compras parceladas", exception);
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

    private String[] fieldTypeNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getType)
                .map(Class::getName)
                .toArray(String[]::new);
    }

    private long uuidParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count();
    }

    private long integerParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes())
                .filter(type -> type.equals(Integer.TYPE) || type.equals(Integer.class))
                .count();
    }
}
