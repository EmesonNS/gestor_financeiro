package com.zorysa.finance.creditcards.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreditCardRequestValidationTest {

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
    void shouldRejectCreateCreditCardWithoutRequiredFields() {
        Object request = instantiateRecord(
                "com.zorysa.finance.creditcards.dto.CreateCreditCardRequest",
                "",
                null,
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "limitAmount", "closingDay", "dueDay");
    }

    @Test
    void shouldRejectCreateCreditCardWithNegativeLimitAndInvalidDays() {
        Object request = instantiateRecord(
                "com.zorysa.finance.creditcards.dto.CreateCreditCardRequest",
                "Visa",
                new BigDecimal("-1.00"),
                0,
                32
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("limitAmount", "closingDay", "dueDay");
    }

    @Test
    void shouldAcceptCreateCreditCardWithZeroLimitAndValidDays() {
        Object request = instantiateRecord(
                "com.zorysa.finance.creditcards.dto.CreateCreditCardRequest",
                "Visa",
                BigDecimal.ZERO,
                1,
                31
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectUpdateCreditCardWithNegativeLimitAndInvalidDays() {
        Object request = instantiateRecord(
                "com.zorysa.finance.creditcards.dto.UpdateCreditCardRequest",
                "Visa",
                new BigDecimal("-1.00"),
                32,
                0
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("limitAmount", "closingDay", "dueDay");
    }

    @Test
    void shouldExposeCreditCardResponseWithLimitSummaryComponents() {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.creditcards.dto.CreditCardResponse");
            RecordComponent[] components = type.getRecordComponents();

            assertThat(components)
                    .as("CreditCardResponse deve ser record para expor limite total, usado e disponivel")
                    .isNotNull();
            assertThat(components).extracting(RecordComponent::getName)
                    .contains(
                            "id",
                            "name",
                            "limitAmount",
                            "usedLimit",
                            "availableLimit",
                            "closingDay",
                            "dueDay",
                            "archived",
                            "createdAt",
                            "updatedAt"
                    );
            assertThat(componentType(type, "limitAmount")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "usedLimit")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "availableLimit")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "archived")).isIn(Boolean.TYPE, Boolean.class);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("CreditCardResponse deve existir para cartoes de credito", exception);
        }
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

    private Class<?> componentType(Class<?> recordType, String componentName) {
        return Arrays.stream(recordType.getRecordComponents())
                .filter(component -> component.getName().equals(componentName))
                .map(RecordComponent::getType)
                .findFirst()
                .orElseThrow(() -> new AssertionError(recordType.getSimpleName() + " deve expor " + componentName));
    }
}
