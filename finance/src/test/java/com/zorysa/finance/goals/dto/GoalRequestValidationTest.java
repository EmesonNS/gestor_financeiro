package com.zorysa.finance.goals.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GoalRequestValidationTest {

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
    void shouldRejectCreateGoalWithoutRequiredFields() {
        Object request = instantiateRecord(
                "com.zorysa.finance.goals.dto.CreateGoalRequest",
                "",
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "targetAmount", "currentAmount");
    }

    @Test
    void shouldRejectCreateGoalWithNonPositiveTargetAndNegativeCurrentAmount() {
        Object request = instantiateRecord(
                "com.zorysa.finance.goals.dto.CreateGoalRequest",
                "Reserva",
                BigDecimal.ZERO,
                new BigDecimal("-1.00"),
                LocalDate.of(2026, 12, 31),
                ""
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("targetAmount", "currentAmount");
    }

    @Test
    void shouldAcceptCreateGoalWithoutDeadlineAndDescription() {
        Object request = instantiateRecord(
                "com.zorysa.finance.goals.dto.CreateGoalRequest",
                "Reserva",
                new BigDecimal("10000.00"),
                BigDecimal.ZERO,
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectUpdateGoalWithInvalidAmounts() {
        Object request = instantiateRecord(
                "com.zorysa.finance.goals.dto.UpdateGoalRequest",
                "Reserva",
                new BigDecimal("-10.00"),
                new BigDecimal("-1.00"),
                LocalDate.of(2026, 12, 31),
                "Reserva atualizada"
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("targetAmount", "currentAmount");
    }

    @Test
    void shouldRejectProgressUpdateWithNegativeCurrentAmount() {
        Object request = instantiateRecord(
                "com.zorysa.finance.goals.dto.UpdateGoalProgressRequest",
                new BigDecimal("-1.00")
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("currentAmount");
    }

    @Test
    void shouldExposeGoalResponseWithStatusAndProgressComponents() {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.goals.dto.GoalResponse");
            RecordComponent[] components = type.getRecordComponents();

            assertThat(components)
                    .as("GoalResponse deve ser record para expor meta e progresso calculado")
                    .isNotNull();
            assertThat(components).extracting(RecordComponent::getName)
                    .contains(
                            "id",
                            "name",
                            "targetAmount",
                            "currentAmount",
                            "deadline",
                            "description",
                            "status",
                            "completionPercentage",
                            "createdAt",
                            "updatedAt"
                    );
            assertThat(componentType(type, "targetAmount")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "currentAmount")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "completionPercentage")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "status").getName()).isEqualTo("com.zorysa.finance.goals.entity.GoalStatus");
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("GoalResponse deve existir para metas financeiras", exception);
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
