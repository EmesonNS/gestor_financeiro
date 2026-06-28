package com.zorysa.finance.transactions.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TransactionRequestValidationTest {

    private static final UUID CATEGORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ACCOUNT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectCreateTransactionWithoutRequiredFields() {
        Object request = instantiateRecord(
                "com.zorysa.finance.transactions.dto.CreateTransactionRequest",
                "",
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                null,
                ""
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("description", "amount", "type", "transactionDate", "categoryId", "status");
    }

    @Test
    void shouldRejectUpdateTransactionWithNegativeAmount() {
        Object request = instantiateRecord(
                "com.zorysa.finance.transactions.dto.UpdateTransactionRequest",
                "Mercado",
                new BigDecimal("-1.00"),
                enumValue("TransactionType", "EXPENSE"),
                LocalDate.of(2026, 6, 20),
                CATEGORY_ID,
                ACCOUNT_ID,
                enumValue("TransactionStatus", "PAID"),
                ""
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("amount");
    }

    @Test
    void shouldAcceptPendingTransactionWithoutAccount() {
        Object request = instantiateRecord(
                "com.zorysa.finance.transactions.dto.CreateTransactionRequest",
                "Compra futura",
                new BigDecimal("150.25"),
                enumValue("TransactionType", "EXPENSE"),
                LocalDate.of(2026, 6, 20),
                CATEGORY_ID,
                null,
                enumValue("TransactionStatus", "PENDING"),
                ""
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    private Object instantiateRecord(String className, Object... values) {
        try {
            Class<?> type = Class.forName(className);
            RecordComponent[] components = type.getRecordComponents();
            assertThat(components)
                    .as(className + " deve ser record para expor o contrato de request")
                    .hasSize(values.length);

            Class<?>[] parameterTypes = new Class<?>[components.length];
            for (int index = 0; index < components.length; index++) {
                parameterTypes[index] = components[index].getType();
            }

            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            return constructor.newInstance(values);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(className + " deve existir com componentes documentados", exception);
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
}
