package com.zorysa.finance.invoices.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InvoiceDomainContractTest {

    private static final UUID USER_ID = UUID.fromString("21212121-2121-2121-2121-212121212121");
    private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CARD_ID = UUID.fromString("23232323-2323-2323-2323-232323232323");
    private static final UUID ACCOUNT_ID = UUID.fromString("24242424-2424-2424-2424-242424242424");

    @Test
    void shouldExposeDocumentedInvoiceStatuses() {
        Class<?> status = findRequiredClass("com.zorysa.finance.invoices.entity.InvoiceStatus");

        assertThat(status.getEnumConstants())
                .extracting(Object::toString)
                .containsExactly("OPEN", "CLOSED", "PAID", "OVERDUE");
    }

    @Test
    void shouldCreateInvoiceForReferencePeriodWithDatesTotalAndStatus() {
        Object invoice = invoice(new BigDecimal("350.00"), "OPEN");

        assertThat(value(invoice, "getUserId")).isEqualTo(USER_ID);
        assertThat(value(invoice, "getCreditCardId")).isEqualTo(CARD_ID);
        assertThat(value(invoice, "getReferenceMonth")).isEqualTo(6);
        assertThat(value(invoice, "getReferenceYear")).isEqualTo(2026);
        assertThat(value(invoice, "getClosingDate")).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(value(invoice, "getDueDate")).isEqualTo(LocalDate.of(2026, 6, 17));
        assertThat(money(invoice, "getTotalAmount")).isEqualByComparingTo("350.00");
        assertThat(value(invoice, "getStatus").toString()).isEqualTo("OPEN");
    }

    @Test
    void shouldUpdateTotalAmountFromInstallments() {
        Object invoice = invoice(BigDecimal.ZERO, "OPEN");

        invoke(invoice, "updateTotalAmount", new BigDecimal("499.90"));

        assertThat(money(invoice, "getTotalAmount")).isEqualByComparingTo("499.90");
    }

    @Test
    void shouldMarkInvoiceAsPaidWithAccountAndPaymentDate() {
        Object invoice = invoice(new BigDecimal("350.00"), "OPEN");

        invoke(invoice, "markAsPaid", ACCOUNT_ID, LocalDate.of(2026, 6, 20));

        assertThat(value(invoice, "getStatus").toString()).isEqualTo("PAID");
        assertThat(value(invoice, "getPaymentAccountId")).isEqualTo(ACCOUNT_ID);
        assertThat(value(invoice, "getPaidAt")).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(booleanValue(invoice, "isPaid")).isTrue();
    }

    @Test
    void shouldBlockDuplicatePaymentInDomainContract() {
        Object invoice = invoice(new BigDecimal("350.00"), "PAID");

        assertThat(booleanValue(invoice, "canBePaid")).isFalse();
    }

    @Test
    void shouldAllowInstallmentsOnlyWhenInvoiceIsNotPaid() {
        Object openInvoice = invoice(new BigDecimal("350.00"), "OPEN");
        Object paidInvoice = invoice(new BigDecimal("350.00"), "PAID");

        assertThat(booleanValue(openInvoice, "canReceiveInstallments")).isTrue();
        assertThat(booleanValue(paidInvoice, "canReceiveInstallments")).isFalse();
    }

    @Test
    void shouldBelongOnlyToOwner() {
        Object invoice = invoice(new BigDecimal("350.00"), "OPEN");

        assertThat(booleanValue(invoice, "belongsTo", USER_ID)).isTrue();
        assertThat(booleanValue(invoice, "belongsTo", OTHER_USER_ID)).isFalse();
    }

    private Object invoice(BigDecimal totalAmount, String statusName) {
        try {
            Class<?> invoiceClass = Class.forName("com.zorysa.finance.invoices.entity.CreditCardInvoice");
            Constructor<?> constructor = invoiceClass.getConstructor(
                    UUID.class,
                    UUID.class,
                    Integer.TYPE,
                    Integer.TYPE,
                    LocalDate.class,
                    LocalDate.class,
                    BigDecimal.class,
                    statusClass()
            );
            return constructor.newInstance(
                    USER_ID,
                    CARD_ID,
                    6,
                    2026,
                    LocalDate.of(2026, 6, 10),
                    LocalDate.of(2026, 6, 17),
                    totalAmount,
                    enumValue(statusName)
            );
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("CreditCardInvoice deve expor construtor com userId, creditCardId, referencia, datas, total e status", exception);
        }
    }

    private Object enumValue(String value) {
        return Enum.valueOf(statusClass().asSubclass(Enum.class), value);
    }

    private Class<?> statusClass() {
        return findRequiredClass("com.zorysa.finance.invoices.entity.InvoiceStatus");
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para faturas de cartao", exception);
        }
    }

    private Object value(Object target, String methodName) {
        return invoke(target, methodName);
    }

    private BigDecimal money(Object target, String methodName, Object... args) {
        return (BigDecimal) invoke(target, methodName, args);
    }

    private boolean booleanValue(Object target, String methodName, Object... args) {
        return (boolean) invoke(target, methodName, args);
    }

    private Object invoke(Object target, String methodName, Object... args) {
        try {
            Method method = method(target.getClass(), methodName, args);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(target.getClass().getSimpleName() + " deve expor metodo " + methodName, exception);
        }
    }

    private Method method(Class<?> type, String methodName, Object[] args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int index = 0; index < args.length; index++) {
            parameterTypes[index] = args[index].getClass();
        }
        try {
            return type.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName, exception);
        }
    }
}
