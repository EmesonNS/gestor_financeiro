package com.zorysa.finance.installments.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InstallmentDomainContractTest {

    private static final UUID USER_ID = UUID.fromString("28282828-2828-2828-2828-282828282828");
    private static final UUID CARD_ID = UUID.fromString("29292929-2929-2929-2929-292929292929");
    private static final UUID CATEGORY_ID = UUID.fromString("30303030-3030-3030-3030-303030303030");
    private static final UUID PURCHASE_ID = UUID.fromString("31313131-3131-3131-3131-313131313131");
    private static final UUID INVOICE_ID = UUID.fromString("32323232-3232-3232-3232-323232323232");

    @Test
    void shouldExposeDocumentedStatuses() {
        assertThat(enumConstants("com.zorysa.finance.installments.entity.PurchaseStatus"))
                .contains("ACTIVE", "CANCELED");
        assertThat(enumConstants("com.zorysa.finance.installments.entity.InstallmentStatus"))
                .containsExactly("OPEN", "PAID", "CANCELED");
    }

    @Test
    void shouldCreateCardPurchaseWithDocumentedFields() {
        Object purchase = purchase(new BigDecimal("3000.00"), 10);

        assertThat(value(purchase, "getUserId")).isEqualTo(USER_ID);
        assertThat(value(purchase, "getCreditCardId")).isEqualTo(CARD_ID);
        assertThat(value(purchase, "getCategoryId")).isEqualTo(CATEGORY_ID);
        assertThat(value(purchase, "getDescription")).isEqualTo("Notebook");
        assertThat(money(purchase, "getTotalAmount")).isEqualByComparingTo("3000.00");
        assertThat(value(purchase, "getInstallmentCount")).isEqualTo(10);
        assertThat(value(purchase, "getStatus").toString()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldCancelPurchase() {
        Object purchase = purchase(new BigDecimal("3000.00"), 10);

        invoke(purchase, "cancel");

        assertThat(value(purchase, "getStatus").toString()).isEqualTo("CANCELED");
    }

    @Test
    void shouldCreateInstallmentWithCompetenceAndInvoice() {
        Object installment = installment(1, 10, new BigDecimal("300.00"), 6, 2026);

        assertThat(value(installment, "getPurchaseId")).isEqualTo(PURCHASE_ID);
        assertThat(value(installment, "getInvoiceId")).isEqualTo(INVOICE_ID);
        assertThat(value(installment, "getInstallmentNumber")).isEqualTo(1);
        assertThat(value(installment, "getTotalInstallments")).isEqualTo(10);
        assertThat(money(installment, "getAmount")).isEqualByComparingTo("300.00");
        assertThat(value(installment, "getCompetenceMonth")).isEqualTo(6);
        assertThat(value(installment, "getCompetenceYear")).isEqualTo(2026);
        assertThat(value(installment, "getStatus").toString()).isEqualTo("OPEN");
    }

    @Test
    void shouldMarkInstallmentAsPaidAndCanceled() {
        Object installment = installment(1, 10, new BigDecimal("300.00"), 6, 2026);

        invoke(installment, "markAsPaid");
        assertThat(value(installment, "getStatus").toString()).isEqualTo("PAID");
        invoke(installment, "cancel");
        assertThat(value(installment, "getStatus").toString()).isEqualTo("CANCELED");
    }

    @Test
    void shouldBelongOnlyToOwner() {
        Object purchase = purchase(new BigDecimal("3000.00"), 10);
        Object installment = installment(1, 10, new BigDecimal("300.00"), 6, 2026);

        assertThat(booleanValue(purchase, "belongsTo", USER_ID)).isTrue();
        assertThat(booleanValue(installment, "belongsTo", USER_ID)).isTrue();
        assertThat(booleanValue(purchase, "belongsTo", UUID.randomUUID())).isFalse();
    }

    private Object purchase(BigDecimal totalAmount, int count) {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.installments.entity.CardPurchase");
            Constructor<?> constructor = type.getConstructor(UUID.class, UUID.class, UUID.class, String.class,
                    BigDecimal.class, LocalDate.class, Integer.TYPE, String.class);
            return constructor.newInstance(USER_ID, CARD_ID, CATEGORY_ID, "Notebook", totalAmount,
                    LocalDate.of(2026, 6, 20), count, null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("CardPurchase deve expor construtor documentado", exception);
        }
    }

    private Object installment(int number, int total, BigDecimal amount, int month, int year) {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.installments.entity.CreditCardInstallment");
            Constructor<?> constructor = type.getConstructor(UUID.class, UUID.class, UUID.class, Integer.TYPE,
                    Integer.TYPE, BigDecimal.class, Integer.TYPE, Integer.TYPE, statusClass());
            return constructor.newInstance(USER_ID, PURCHASE_ID, INVOICE_ID, number, total, amount, month, year, enumValue("OPEN"));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("CreditCardInstallment deve expor construtor documentado", exception);
        }
    }

    private Class<?> statusClass() {
        return findRequiredClass("com.zorysa.finance.installments.entity.InstallmentStatus");
    }

    private Object enumValue(String value) {
        return Enum.valueOf(statusClass().asSubclass(Enum.class), value);
    }

    private Object value(Object target, String methodName) {
        return invoke(target, methodName);
    }

    private BigDecimal money(Object target, String methodName) {
        return (BigDecimal) invoke(target, methodName);
    }

    private boolean booleanValue(Object target, String methodName, Object... args) {
        return (boolean) invoke(target, methodName, args);
    }

    private Object invoke(Object target, String methodName, Object... args) {
        try {
            Class<?>[] types = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
            Method method = target.getClass().getMethod(methodName, types);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(target.getClass().getSimpleName() + " deve expor metodo " + methodName, exception);
        }
    }

    private String[] enumConstants(String className) {
        return Arrays.stream(findRequiredClass(className).getEnumConstants()).map(Object::toString).toArray(String[]::new);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para compras parceladas", exception);
        }
    }
}
