package com.zorysa.finance.accounts.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AccountRequestValidationTest {

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
    void shouldRejectCreateAccountWithoutNameTypeAndInitialBalance() {
        Object request = instantiateRecord(
                "com.zorysa.finance.accounts.dto.CreateAccountRequest",
                "",
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "type", "initialBalance");
    }

    @Test
    void shouldAcceptCreateAccountWithZeroInitialBalance() {
        Object request = instantiateRecord(
                "com.zorysa.finance.accounts.dto.CreateAccountRequest",
                "Carteira",
                enumValue("CASH_WALLET"),
                BigDecimal.ZERO
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectUpdateAccountWithoutNameAndType() {
        Object request = instantiateRecord(
                "com.zorysa.finance.accounts.dto.UpdateAccountRequest",
                "",
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "type");
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

    private Object enumValue(String value) {
        try {
            Class<?> accountType = Class.forName("com.zorysa.finance.accounts.entity.AccountType");
            return Enum.valueOf(accountType.asSubclass(Enum.class), value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("AccountType deve existir com valor " + value, exception);
        }
    }
}
