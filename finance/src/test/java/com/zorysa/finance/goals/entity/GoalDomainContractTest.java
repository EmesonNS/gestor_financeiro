package com.zorysa.finance.goals.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GoalDomainContractTest {

    private static final UUID USER_ID = UUID.fromString("13131313-1313-1313-1313-131313131313");
    private static final UUID OTHER_USER_ID = UUID.fromString("14141414-1414-1414-1414-141414141414");

    @Test
    void shouldExposeDocumentedGoalStatuses() {
        Class<?> status = findRequiredClass("com.zorysa.finance.goals.entity.GoalStatus");

        assertThat(status.getEnumConstants())
                .extracting(Object::toString)
                .containsExactly("ACTIVE", "COMPLETED", "CANCELED");
    }

    @Test
    void shouldCreateActiveGoalWithTargetCurrentDeadlineAndDescription() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("1000.00"));

        assertThat(value(goal, "getUserId")).isEqualTo(USER_ID);
        assertThat(value(goal, "getName")).isEqualTo("Reserva");
        assertThat(money(goal, "getTargetAmount")).isEqualByComparingTo("10000.00");
        assertThat(money(goal, "getCurrentAmount")).isEqualByComparingTo("1000.00");
        assertThat(value(goal, "getDeadline")).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(value(goal, "getDescription")).isEqualTo("Reserva de emergencia");
        assertThat(value(goal, "getStatus").toString()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldCalculateCompletionPercentage() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("2500.00"));

        assertThat(money(goal, "completionPercentage")).isEqualByComparingTo("25.00");
    }

    @Test
    void shouldCapCompletionPercentageAtOneHundred() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("12000.00"));

        assertThat(money(goal, "completionPercentage")).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldUpdateProgressAndMarkAsCompletedWhenTargetIsReached() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("1000.00"));

        invoke(goal, "updateProgress", new BigDecimal("10000.00"));

        assertThat(money(goal, "getCurrentAmount")).isEqualByComparingTo("10000.00");
        assertThat(value(goal, "getStatus").toString()).isEqualTo("COMPLETED");
        assertThat(money(goal, "completionPercentage")).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldKeepGoalActiveWhenProgressIsBelowTarget() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("1000.00"));

        invoke(goal, "updateProgress", new BigDecimal("5000.00"));

        assertThat(value(goal, "getStatus").toString()).isEqualTo("ACTIVE");
        assertThat(money(goal, "completionPercentage")).isEqualByComparingTo("50.00");
    }

    @Test
    void shouldCancelGoal() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("1000.00"));

        invoke(goal, "cancel");

        assertThat(value(goal, "getStatus").toString()).isEqualTo("CANCELED");
    }

    @Test
    void shouldBelongOnlyToOwner() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("1000.00"));

        assertThat(booleanValue(goal, "belongsTo", USER_ID)).isTrue();
        assertThat(booleanValue(goal, "belongsTo", OTHER_USER_ID)).isFalse();
    }

    @Test
    void shouldUpdateGoalDetailsAndRecalculateStatusFromCurrentAmount() {
        Object goal = goal(new BigDecimal("10000.00"), new BigDecimal("1000.00"));

        invoke(
                goal,
                "updateDetails",
                "Reserva atualizada",
                new BigDecimal("12000.00"),
                new BigDecimal("12000.00"),
                LocalDate.of(2027, 1, 31),
                "Nova descricao"
        );

        assertThat(value(goal, "getName")).isEqualTo("Reserva atualizada");
        assertThat(money(goal, "getTargetAmount")).isEqualByComparingTo("12000.00");
        assertThat(money(goal, "getCurrentAmount")).isEqualByComparingTo("12000.00");
        assertThat(value(goal, "getDeadline")).isEqualTo(LocalDate.of(2027, 1, 31));
        assertThat(value(goal, "getStatus").toString()).isEqualTo("COMPLETED");
    }

    private Object goal(BigDecimal targetAmount, BigDecimal currentAmount) {
        try {
            Class<?> goalClass = Class.forName("com.zorysa.finance.goals.entity.Goal");
            Constructor<?> constructor = goalClass.getConstructor(
                    UUID.class,
                    String.class,
                    BigDecimal.class,
                    BigDecimal.class,
                    LocalDate.class,
                    String.class
            );
            return constructor.newInstance(
                    USER_ID,
                    "Reserva",
                    targetAmount,
                    currentAmount,
                    LocalDate.of(2026, 12, 31),
                    "Reserva de emergencia"
            );
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Goal deve expor construtor com userId, name, targetAmount, currentAmount, deadline e description", exception);
        }
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para metas financeiras", exception);
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
