package com.zorysa.finance.accounts.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountDomainContractTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Test
    void shouldCreateAccountForOwnerWithCurrentBalanceEqualToInitialBalance() {
        Object type = enumValue("DIGITAL_ACCOUNT");
        Object account = instantiateAccount(OWNER_ID, "Nubank", type, new BigDecimal("1000.00"));

        assertThat(invoke(account, "getUserId")).isEqualTo(OWNER_ID);
        assertThat(invoke(account, "getName")).isEqualTo("Nubank");
        assertThat(invoke(account, "getType")).isEqualTo(type);
        assertThat(money(account, "getInitialBalance")).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(money(account, "getCurrentBalance")).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(invoke(account, "isArchived")).isEqualTo(false);
    }

    @Test
    void shouldKnowWhetherAccountBelongsToAuthenticatedUser() {
        Object account = instantiateAccount(OWNER_ID, "Carteira", enumValue("CASH_WALLET"), BigDecimal.ZERO);

        assertThat(invoke(account, "belongsTo", OWNER_ID)).isEqualTo(true);
        assertThat(invoke(account, "belongsTo", OTHER_USER_ID)).isEqualTo(false);
    }

    @Test
    void shouldUpdateEditableAccountDetailsWithoutChangingBalancesOrOwner() {
        Object account = instantiateAccount(OWNER_ID, "Antiga", enumValue("CHECKING_ACCOUNT"), new BigDecimal("250.00"));

        invoke(account, "updateDetails", "Conta principal", enumValue("SAVINGS_ACCOUNT"));

        assertThat(invoke(account, "getUserId")).isEqualTo(OWNER_ID);
        assertThat(invoke(account, "getName")).isEqualTo("Conta principal");
        assertThat(invoke(account, "getType")).isEqualTo(enumValue("SAVINGS_ACCOUNT"));
        assertThat(money(account, "getInitialBalance")).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(money(account, "getCurrentBalance")).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldArchiveAccount() {
        Object account = instantiateAccount(OWNER_ID, "Poupanca", enumValue("SAVINGS_ACCOUNT"), BigDecimal.TEN);

        invoke(account, "archive");

        assertThat(invoke(account, "isArchived")).isEqualTo(true);
    }

    private Object instantiateAccount(UUID userId, String name, Object type, BigDecimal initialBalance) {
        try {
            Class<?> accountClass = Class.forName("com.zorysa.finance.accounts.entity.Account");
            return accountClass
                    .getDeclaredConstructor(UUID.class, String.class, type.getClass(), BigDecimal.class)
                    .newInstance(userId, name, type, initialBalance);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Account deve expor construtor (UUID userId, String name, AccountType type, BigDecimal initialBalance)",
                    exception
            );
        }
    }

    private Object enumValue(String value) {
        try {
            Class<?> accountType = Class.forName("com.zorysa.finance.accounts.entity.AccountType");
            return Enum.valueOf(accountType.asSubclass(Enum.class), value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("AccountType deve existir com valor " + value, exception);
        }
    }

    private Object invoke(Object target, String methodName, Object... args) {
        Method method = Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .filter(candidate -> candidate.getParameterCount() == args.length)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account deve expor metodo " + methodName));
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Falha ao executar Account." + methodName, exception);
        }
    }
    private BigDecimal money(Object target, String methodName) {
        Object value = invoke(target, methodName);
        assertThat(value).as(methodName + " deve retornar BigDecimal").isInstanceOf(BigDecimal.class);
        return (BigDecimal) value;
    }
}
