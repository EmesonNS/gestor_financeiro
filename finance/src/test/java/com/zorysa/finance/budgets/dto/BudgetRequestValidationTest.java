package com.zorysa.finance.budgets.dto;

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
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BudgetRequestValidationTest {

    private static final UUID CATEGORY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

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
    void shouldRejectCreateBudgetWithoutRequiredFields() {
        Object request = instantiateRecord(
                "com.zorysa.finance.budgets.dto.CreateBudgetRequest",
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("categoryId", "startMonth", "startYear", "limitAmount");
    }

    @Test
    void shouldRejectCreateBudgetWithInvalidMonthsAndNonPositiveLimit() {
        Object request = instantiateRecord(
                "com.zorysa.finance.budgets.dto.CreateBudgetRequest",
                CATEGORY_ID,
                13,
                2026,
                0,
                2026,
                BigDecimal.ZERO
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("startMonth", "endMonth", "limitAmount");
    }

    @Test
    void shouldAcceptSingleMonthBudgetWhenStartAndEndAreSameMonth() {
        Object request = instantiateRecord(
                "com.zorysa.finance.budgets.dto.CreateBudgetRequest",
                CATEGORY_ID,
                6,
                2026,
                6,
                2026,
                new BigDecimal("1000.00")
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldAcceptMultiMonthBudget() {
        Object request = instantiateRecord(
                "com.zorysa.finance.budgets.dto.CreateBudgetRequest",
                CATEGORY_ID,
                6,
                2026,
                12,
                2026,
                new BigDecimal("1000.00")
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldAcceptOpenEndedBudgetWhenEndMonthAndEndYearAreNull() {
        Object request = instantiateRecord(
                "com.zorysa.finance.budgets.dto.CreateBudgetRequest",
                CATEGORY_ID,
                6,
                2026,
                null,
                null,
                new BigDecimal("1000.00")
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectUpdateBudgetWithInvalidMonthsAndNegativeLimit() {
        Object request = instantiateRecord(
                "com.zorysa.finance.budgets.dto.UpdateBudgetRequest",
                CATEGORY_ID,
                0,
                2026,
                13,
                2026,
                new BigDecimal("-1.00")
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("startMonth", "endMonth", "limitAmount");
    }

    @Test
    void shouldExposeBudgetResponseWithPeriodAndCalculatedProgressComponents() {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.budgets.dto.BudgetResponse");
            RecordComponent[] components = type.getRecordComponents();

            assertThat(components)
                    .as("BudgetResponse deve ser record para expor periodo, dados planejados e calculados")
                    .isNotNull();
            assertThat(components).extracting(RecordComponent::getName)
                    .contains(
                            "id",
                            "categoryId",
                            "startMonth",
                            "startYear",
                            "endMonth",
                            "endYear",
                            "limitAmount",
                            "spentAmount",
                            "remainingAmount",
                            "usagePercentage",
                            "exceeded",
                            "createdAt",
                            "updatedAt"
                    );
            assertThat(componentType(type, "limitAmount")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "spentAmount")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "remainingAmount")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "usagePercentage")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "exceeded")).isIn(Boolean.TYPE, Boolean.class);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("BudgetResponse deve existir para orcamentos", exception);
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
