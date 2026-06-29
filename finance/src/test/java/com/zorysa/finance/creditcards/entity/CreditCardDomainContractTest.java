package com.zorysa.finance.creditcards.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreditCardDomainContractTest {

    private static final UUID USER_ID = UUID.fromString("16161616-1616-1616-1616-161616161616");
    private static final UUID OTHER_USER_ID = UUID.fromString("17171717-1717-1717-1717-171717171717");

    @Test
    void shouldCreateCreditCardWithLimitClosingDueAndNotArchived() {
        Object card = card(new BigDecimal("5000.00"));

        assertThat(value(card, "getUserId")).isEqualTo(USER_ID);
        assertThat(value(card, "getName")).isEqualTo("Visa");
        assertThat(money(card, "getLimitAmount")).isEqualByComparingTo("5000.00");
        assertThat(value(card, "getClosingDay")).isEqualTo(10);
        assertThat(value(card, "getDueDay")).isEqualTo(17);
        assertThat(booleanValue(card, "isArchived")).isFalse();
    }

    @Test
    void shouldCalculateUsedAndAvailableLimit() {
        Object card = card(new BigDecimal("5000.00"));

        assertThat(money(card, "usedLimit", new BigDecimal("1250.50"))).isEqualByComparingTo("1250.50");
        assertThat(money(card, "availableLimit", new BigDecimal("1250.50"))).isEqualByComparingTo("3749.50");
    }

    @Test
    void shouldNotReturnNegativeAvailableLimitWhenUsedLimitExceedsTotalLimit() {
        Object card = card(new BigDecimal("1000.00"));

        assertThat(money(card, "availableLimit", new BigDecimal("1500.00"))).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldArchiveCreditCard() {
        Object card = card(new BigDecimal("5000.00"));

        invoke(card, "archive");

        assertThat(booleanValue(card, "isArchived")).isTrue();
        assertThat(booleanValue(card, "canReceivePurchases")).isFalse();
    }

    @Test
    void shouldAllowPurchasesOnlyWhenNotArchived() {
        Object card = card(new BigDecimal("5000.00"));

        assertThat(booleanValue(card, "canReceivePurchases")).isTrue();
        invoke(card, "archive");
        assertThat(booleanValue(card, "canReceivePurchases")).isFalse();
    }

    @Test
    void shouldBelongOnlyToOwner() {
        Object card = card(new BigDecimal("5000.00"));

        assertThat(booleanValue(card, "belongsTo", USER_ID)).isTrue();
        assertThat(booleanValue(card, "belongsTo", OTHER_USER_ID)).isFalse();
    }

    @Test
    void shouldUpdateCreditCardDetails() {
        Object card = card(new BigDecimal("5000.00"));

        invoke(card, "updateDetails", "Visa Platinum", new BigDecimal("7500.00"), 12, 20);

        assertThat(value(card, "getName")).isEqualTo("Visa Platinum");
        assertThat(money(card, "getLimitAmount")).isEqualByComparingTo("7500.00");
        assertThat(value(card, "getClosingDay")).isEqualTo(12);
        assertThat(value(card, "getDueDay")).isEqualTo(20);
    }

    private Object card(BigDecimal limitAmount) {
        try {
            Class<?> cardClass = Class.forName("com.zorysa.finance.creditcards.entity.CreditCard");
            Constructor<?> constructor = cardClass.getConstructor(
                    UUID.class,
                    String.class,
                    BigDecimal.class,
                    Integer.TYPE,
                    Integer.TYPE
            );
            return constructor.newInstance(USER_ID, "Visa", limitAmount, 10, 17);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("CreditCard deve expor construtor com userId, name, limitAmount, closingDay e dueDay", exception);
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
            parameterTypes[index] = args[index] instanceof Integer ? Integer.TYPE : args[index].getClass();
        }
        try {
            return type.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName, exception);
        }
    }
}
