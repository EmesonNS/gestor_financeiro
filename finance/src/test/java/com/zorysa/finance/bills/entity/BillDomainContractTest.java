package com.zorysa.finance.bills.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BillDomainContractTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID CATEGORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ACCOUNT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID TRANSACTION_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @Test
    void shouldCreatePendingBillForAuthenticatedUser() {
        Object bill = bill("PENDING", null, null, null);

        assertThat(invoke(bill, "getUserId")).isEqualTo(OWNER_ID);
        assertThat(invoke(bill, "getDescription")).isEqualTo("Energia");
        assertThat(money(bill, "getAmount")).isEqualByComparingTo(new BigDecimal("180.00"));
        assertThat(invoke(bill, "getDueDate")).isEqualTo(LocalDate.of(2026, 6, 25));
        assertThat(invoke(bill, "getCategoryId")).isEqualTo(CATEGORY_ID);
        assertThat(invoke(bill, "getAccountId")).isNull();
        assertThat(invoke(bill, "getStatus")).isEqualTo(enumValue("PENDING"));
        assertThat(invoke(bill, "belongsTo", OWNER_ID)).isEqualTo(true);
        assertThat(invoke(bill, "belongsTo", OTHER_USER_ID)).isEqualTo(false);
    }

    @Test
    void shouldIdentifyOverdueAndUpcomingBills() {
        Object bill = bill("PENDING", null, null, null);

        assertThat(invoke(bill, "isOverdue", LocalDate.of(2026, 6, 26))).isEqualTo(true);
        assertThat(invoke(bill, "isUpcoming", LocalDate.of(2026, 6, 20), 7)).isEqualTo(true);
    }

    @Test
    void shouldMarkPendingBillAsPaidWithAccountPaymentDateAndTransaction() {
        Object bill = bill("PENDING", null, null, null);

        invoke(bill, "markAsPaid", ACCOUNT_ID, LocalDate.of(2026, 6, 20), TRANSACTION_ID);

        assertThat(invoke(bill, "getStatus")).isEqualTo(enumValue("PAID"));
        assertThat(invoke(bill, "getAccountId")).isEqualTo(ACCOUNT_ID);
        assertThat(invoke(bill, "getPaidAt")).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(invoke(bill, "getTransactionId")).isEqualTo(TRANSACTION_ID);
    }

    @Test
    void shouldUpdateEditableBillDetailsWithoutChangingOwner() {
        Object bill = bill("PENDING", null, null, null);

        invoke(
                bill,
                "updateDetails",
                "Internet",
                new BigDecimal("120.00"),
                LocalDate.of(2026, 6, 28),
                CATEGORY_ID,
                ACCOUNT_ID,
                enumValue("PENDING")
        );

        assertThat(invoke(bill, "getUserId")).isEqualTo(OWNER_ID);
        assertThat(invoke(bill, "getDescription")).isEqualTo("Internet");
        assertThat(money(bill, "getAmount")).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(invoke(bill, "getDueDate")).isEqualTo(LocalDate.of(2026, 6, 28));
        assertThat(invoke(bill, "getAccountId")).isEqualTo(ACCOUNT_ID);
    }

    @Test
    void shouldExposeGuardAgainstDuplicatePayment() {
        Object bill = bill("PAID", ACCOUNT_ID, LocalDate.of(2026, 6, 20), TRANSACTION_ID);

        assertThat(invoke(bill, "isPaid")).isEqualTo(true);
    }

    private Object bill(String status, UUID accountId, LocalDate paidAt, UUID transactionId) {
        try {
            Object billStatus = enumValue(status);
            Class<?> billClass = Class.forName("com.zorysa.finance.bills.entity.Bill");
            return billClass
                    .getDeclaredConstructor(
                            UUID.class,
                            String.class,
                            BigDecimal.class,
                            LocalDate.class,
                            UUID.class,
                            UUID.class,
                            billStatus.getClass(),
                            LocalDate.class,
                            UUID.class
                    )
                    .newInstance(
                            OWNER_ID,
                            "Energia",
                            new BigDecimal("180.00"),
                            LocalDate.of(2026, 6, 25),
                            CATEGORY_ID,
                            accountId,
                            billStatus,
                            paidAt,
                            transactionId
                    );
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Bill deve expor construtor (UUID userId, String description, BigDecimal amount, LocalDate dueDate, UUID categoryId, UUID accountId, BillStatus status, LocalDate paidAt, UUID transactionId)",
                    exception
            );
        }
    }

    private Object enumValue(String value) {
        try {
            Class<?> enumClass = Class.forName("com.zorysa.finance.bills.entity.BillStatus");
            return Enum.valueOf(enumClass.asSubclass(Enum.class), value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("BillStatus deve existir com valor " + value, exception);
        }
    }

    private Object invoke(Object target, String methodName, Object... args) {
        Method method = Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .filter(candidate -> candidate.getParameterCount() == args.length)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Bill deve expor metodo " + methodName));
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Falha ao executar Bill." + methodName, exception);
        }
    }

    private BigDecimal money(Object target, String methodName) {
        Object value = invoke(target, methodName);
        assertThat(value).as(methodName + " deve retornar BigDecimal").isInstanceOf(BigDecimal.class);
        return (BigDecimal) value;
    }
}
