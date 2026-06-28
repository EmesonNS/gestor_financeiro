package com.zorysa.finance.transactions.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionDomainContractTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID CATEGORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ACCOUNT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Test
    void shouldCreateTransactionForAuthenticatedUser() {
        Object transaction = transaction("EXPENSE", "PAID", new BigDecimal("150.25"), ACCOUNT_ID);

        assertThat(invoke(transaction, "getUserId")).isEqualTo(OWNER_ID);
        assertThat(invoke(transaction, "getDescription")).isEqualTo("Mercado");
        assertThat(money(transaction, "getAmount")).isEqualByComparingTo(new BigDecimal("150.25"));
        assertThat(invoke(transaction, "getCategoryId")).isEqualTo(CATEGORY_ID);
        assertThat(invoke(transaction, "getAccountId")).isEqualTo(ACCOUNT_ID);
        assertThat(invoke(transaction, "belongsTo", OWNER_ID)).isEqualTo(true);
        assertThat(invoke(transaction, "belongsTo", OTHER_USER_ID)).isEqualTo(false);
    }

    @Test
    void shouldCalculateBalanceImpactOnlyForRealizedTransactions() {
        assertThat(money(transaction("EXPENSE", "PAID", new BigDecimal("150.25"), ACCOUNT_ID), "balanceImpact"))
                .isEqualByComparingTo(new BigDecimal("-150.25"));
        assertThat(money(transaction("INCOME", "RECEIVED", new BigDecimal("250.00"), ACCOUNT_ID), "balanceImpact"))
                .isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(money(transaction("EXPENSE", "PENDING", new BigDecimal("150.25"), null), "balanceImpact"))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(money(transaction("INCOME", "CANCELED", new BigDecimal("250.00"), ACCOUNT_ID), "balanceImpact"))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldMarkPendingTransactionAsPaidWithAccount() {
        Object transaction = transaction("EXPENSE", "PENDING", new BigDecimal("80.00"), null);

        invoke(transaction, "markAsPaid", ACCOUNT_ID);

        assertThat(invoke(transaction, "getStatus")).isEqualTo(enumValue("TransactionStatus", "PAID"));
        assertThat(invoke(transaction, "getAccountId")).isEqualTo(ACCOUNT_ID);
        assertThat(money(transaction, "balanceImpact")).isEqualByComparingTo(new BigDecimal("-80.00"));
    }

    @Test
    void shouldCancelRealizedTransactionAndClearBalanceImpact() {
        Object transaction = transaction("EXPENSE", "PAID", new BigDecimal("80.00"), ACCOUNT_ID);

        invoke(transaction, "cancel");

        assertThat(invoke(transaction, "getStatus")).isEqualTo(enumValue("TransactionStatus", "CANCELED"));
        assertThat(money(transaction, "balanceImpact")).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldUpdateTransactionDetails() {
        Object transaction = transaction("EXPENSE", "PENDING", new BigDecimal("80.00"), null);

        invoke(
                transaction,
                "updateDetails",
                "Salario",
                new BigDecimal("1000.00"),
                enumValue("TransactionType", "INCOME"),
                LocalDate.of(2026, 6, 21),
                CATEGORY_ID,
                ACCOUNT_ID,
                enumValue("TransactionStatus", "RECEIVED"),
                "Pagamento mensal"
        );

        assertThat(invoke(transaction, "getDescription")).isEqualTo("Salario");
        assertThat(money(transaction, "getAmount")).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(invoke(transaction, "getType")).isEqualTo(enumValue("TransactionType", "INCOME"));
        assertThat(invoke(transaction, "getTransactionDate")).isEqualTo(LocalDate.of(2026, 6, 21));
        assertThat(invoke(transaction, "getStatus")).isEqualTo(enumValue("TransactionStatus", "RECEIVED"));
        assertThat(money(transaction, "balanceImpact")).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    private Object transaction(String type, String status, BigDecimal amount, UUID accountId) {
        try {
            Object transactionType = enumValue("TransactionType", type);
            Object transactionStatus = enumValue("TransactionStatus", status);
            Class<?> transactionClass = Class.forName("com.zorysa.finance.transactions.entity.Transaction");
            return transactionClass
                    .getDeclaredConstructor(
                            UUID.class,
                            String.class,
                            BigDecimal.class,
                            transactionType.getClass(),
                            LocalDate.class,
                            UUID.class,
                            UUID.class,
                            transactionStatus.getClass(),
                            String.class
                    )
                    .newInstance(
                            OWNER_ID,
                            "Mercado",
                            amount,
                            transactionType,
                            LocalDate.of(2026, 6, 20),
                            CATEGORY_ID,
                            accountId,
                            transactionStatus,
                            ""
                    );
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Transaction deve expor construtor (UUID userId, String description, BigDecimal amount, TransactionType type, LocalDate transactionDate, UUID categoryId, UUID accountId, TransactionStatus status, String notes)",
                    exception
            );
        }
    }

    private Object enumValue(String simpleName, String value) {
        try {
            Class<?> enumClass = Class.forName("com.zorysa.finance.transactions.entity." + simpleName);
            return Enum.valueOf(enumClass.asSubclass(Enum.class), value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(simpleName + " deve existir com valor " + value, exception);
        }
    }

    private Object invoke(Object target, String methodName, Object... args) {
        Method method = Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .filter(candidate -> candidate.getParameterCount() == args.length)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction deve expor metodo " + methodName));
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Falha ao executar Transaction." + methodName, exception);
        }
    }

    private BigDecimal money(Object target, String methodName) {
        Object value = invoke(target, methodName);
        assertThat(value).as(methodName + " deve retornar BigDecimal").isInstanceOf(BigDecimal.class);
        return (BigDecimal) value;
    }
}
