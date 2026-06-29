package com.zorysa.finance.bills.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class BillServiceContractTest {

    @Test
    void shouldListBillsForAuthenticatedUserWithDocumentedFiltersAndPagination() {
        Method method = method("listBills");

        assertThat(method.getReturnType().getName())
                .as("listBills deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class, LocalDate.class, Boolean.class);
        assertThat(method.getParameterCount())
                .as("listBills deve receber userId autenticado, filtros documentados e pageable")
                .isGreaterThanOrEqualTo(8);
    }

    @Test
    void shouldCreateBillForAuthenticatedUserOnly() {
        Method method = method("createBill");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.bills.dto.BillResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateBillRequest"));
    }

    @Test
    void shouldReadUpdateDeleteAndPayOnlyOwnedBills() {
        assertThat(method("getBill")).satisfies(this::requiresAuthenticatedUserAndBillId);
        assertThat(method("updateBill")).satisfies(this::requiresAuthenticatedUserAndBillId);
        assertThat(method("deleteBill")).satisfies(this::requiresAuthenticatedUserAndBillId);
        assertThat(method("payBill")).satisfies(this::requiresAuthenticatedUserAndBillId);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwnerAndDueFilters() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.bills.repository.BillRepository");

        assertThat(methodNames(repository)).contains(
                "findByIdAndUserId",
                "findAllByUserId",
                "findOverdueByUserId",
                "findUpcomingByUserId"
        );
        assertThat(parameterTypes(method(repository, "findAllByUserId")))
                .as("consulta de colecao de contas a pagar deve aceitar Pageable")
                .contains(Pageable.class);
    }

    @Test
    void shouldExposePaymentIntegrationWithTransactions() {
        Method method = method("payBill");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.bills.dto.BillResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("PayBillRequest"));
    }

    private void requiresAuthenticatedUserAndBillId(Method method) {
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter dois UUIDs: userId e billId")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.bills.service.BillService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.bills.dto." + simpleName);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Contas a pagar", exception);
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
