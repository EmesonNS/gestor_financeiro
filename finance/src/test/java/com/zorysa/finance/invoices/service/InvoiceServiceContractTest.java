package com.zorysa.finance.invoices.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class InvoiceServiceContractTest {

    @Test
    void shouldListInvoicesForOwnedCreditCardWithStatusYearAndPagination() {
        Method method = method("listInvoices");

        assertThat(method.getReturnType().getName())
                .as("listInvoices deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, Pageable.class, statusClass());
        assertThat(integerParameterCount(method))
                .as("listInvoices deve aceitar filtro year")
                .isGreaterThanOrEqualTo(1);
        assertThat(method.getParameterCount())
                .as("listInvoices deve receber userId, cardId, status, year e pageable")
                .isGreaterThanOrEqualTo(5);
    }

    @Test
    void shouldReadCurrentInvoiceAndInvoiceDetailsOnlyForAuthenticatedUser() {
        assertThat(method("getCurrentInvoice")).satisfies(this::requiresAuthenticatedUserAndResourceId);
        assertThat(method("getInvoice")).satisfies(this::requiresAuthenticatedUserAndResourceId);
    }

    @Test
    void shouldPayOnlyOwnedInvoiceWithPaymentRequest() {
        Method method = method("payInvoice");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.invoices.dto.InvoiceResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("PayInvoiceRequest"));
        requiresAuthenticatedUserAndResourceId(method);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwnerCardPeriodStatusAndUniqueness() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.invoices.repository.InvoiceRepository");

        assertThat(methodNames(repository)).contains(
                "findByIdAndUserId",
                "findAllByUserIdAndCreditCardId",
                "findByCreditCardIdAndReferenceMonthAndReferenceYear",
                "findCurrentByUserIdAndCreditCardId"
        );
        assertThat(parameterTypes(method(repository, "findAllByUserIdAndCreditCardId")))
                .as("consulta de colecao de faturas deve aceitar Pageable")
                .contains(Pageable.class);
        assertThat(method(repository, "findByCreditCardIdAndReferenceMonthAndReferenceYear").getReturnType().getName())
                .as("busca por competencia deve permitir reutilizar fatura existente")
                .isEqualTo(Optional.class.getName());
    }

    @Test
    void shouldGetOrCreateInvoiceUsingPurchaseDateAndCreditCardClosingDay() {
        Method method = method("getOrCreateInvoice");

        assertThat(method.getReturnType().getName()).contains("CreditCardInvoice");
        assertThat(parameterTypes(method)).contains(UUID.class, LocalDate.class);
        assertThat(integerParameterCount(method))
                .as("getOrCreateInvoice deve considerar closingDay e dueDay do cartao")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldCalculateClosingAndDueDatesFromReferenceAndCardDays() {
        Class<?> service = findRequiredClass("com.zorysa.finance.invoices.service.InvoiceService");

        assertThat(methodNames(service)).contains("calculateClosingDate", "calculateDueDate");
        assertThat(method(service, "calculateClosingDate").getReturnType()).isEqualTo(LocalDate.class);
        assertThat(method(service, "calculateDueDate").getReturnType()).isEqualTo(LocalDate.class);
    }

    @Test
    void shouldCalculateInvoiceTotalFromInstallments() {
        Method method = method("calculateTotalAmount");

        assertThat(method.getReturnType()).isEqualTo(BigDecimal.class);
        assertThat(parameterTypes(method)).contains(UUID.class);
    }

    @Test
    void shouldValidatePaymentRulesAndAdjustAccountBalanceOnlyOnPayment() {
        Class<?> service = findRequiredClass("com.zorysa.finance.invoices.service.InvoiceService");

        assertThat(methodNames(service))
                .as("InvoiceService deve bloquear pagamento duplicado e ajustar saldo apenas no pagamento")
                .contains("validateCanPay", "applyPaymentBalanceImpact");
    }

    @Test
    void shouldRescheduleOnlyUnpaidInvoicesWhenCreditCardBillingDaysChange() {
        Class<?> service = findRequiredClass("com.zorysa.finance.invoices.service.InvoiceService");
        Class<?> repository = findRequiredClass("com.zorysa.finance.invoices.repository.InvoiceRepository");

        assertThat(methodNames(service))
                .as("InvoiceService deve recalcular fechamento/vencimento de faturas não pagas do cartão")
                .contains("rescheduleUnpaidInvoicesForCreditCard", "recalculateInvoiceDates");
        Method method = method(service, "rescheduleUnpaidInvoicesForCreditCard");

        assertThat(method.getReturnType()).isEqualTo(Void.TYPE);
        assertThat(parameterTypes(method)).contains(UUID.class);
        assertThat(integerParameterCount(method))
                .as("rescheduleUnpaidInvoicesForCreditCard deve receber closingDay e dueDay")
                .isGreaterThanOrEqualTo(2);
        assertThat(methodNames(repository))
                .as("InvoiceRepository deve buscar apenas faturas não pagas do cartão do usuário")
                .contains("findAllByUserIdAndCreditCardIdAndStatusNot");
    }

    private void requiresAuthenticatedUserAndResourceId(Method method) {
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter pelo menos userId e id do recurso")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.invoices.service.InvoiceService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.invoices.dto." + simpleName);
    }

    private Class<?> statusClass() {
        return findRequiredClass("com.zorysa.finance.invoices.entity.InvoiceStatus");
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Faturas de cartao", exception);
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
