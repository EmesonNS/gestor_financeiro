package com.zorysa.finance.categories.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CategoryRequestValidationTest {

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
    void shouldRejectCreateCategoryWithoutNameAndType() {
        Object request = instantiateRecord(
                "com.zorysa.finance.categories.dto.CreateCategoryRequest",
                "",
                null,
                "#16a34a",
                "utensils"
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "type");
    }

    @Test
    void shouldAcceptCreateCategoryWithoutOptionalColorAndIcon() {
        Object request = instantiateRecord(
                "com.zorysa.finance.categories.dto.CreateCategoryRequest",
                "Salario",
                enumValue("INCOME"),
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectUpdateCategoryWithoutNameAndType() {
        Object request = instantiateRecord(
                "com.zorysa.finance.categories.dto.UpdateCategoryRequest",
                "",
                null,
                "#15803d",
                "shopping-cart"
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "type");
    }

    @Test
    void shouldExposeCategoryCountResponseWithCountComponent() {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.categories.dto.CategoryCountResponse");
            RecordComponent[] components = type.getRecordComponents();

            assertThat(components)
                    .as("CategoryCountResponse deve ser record com um unico componente count")
                    .hasSize(1);
            assertThat(components[0].getName()).isEqualTo("count");
            assertThat(components[0].getType()).isIn(Long.TYPE, Long.class);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("CategoryCountResponse deve existir para o endpoint de contagem", exception);
        }
    }

    @Test
    void shouldExposeCategoryTypeCountsResponseWithIncomeAndExpenseComponents() {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.categories.dto.CategoryTypeCountsResponse");
            RecordComponent[] components = type.getRecordComponents();

            assertThat(components)
                    .as("CategoryTypeCountsResponse deve ser record com incomeCount e expenseCount")
                    .hasSize(2);
            assertThat(components).extracting(RecordComponent::getName)
                    .containsExactly("incomeCount", "expenseCount");
            assertThat(components[0].getType()).isIn(Long.TYPE, Long.class);
            assertThat(components[1].getType()).isIn(Long.TYPE, Long.class);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("CategoryTypeCountsResponse deve existir para o endpoint de contagem por tipo", exception);
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

    private Object enumValue(String value) {
        try {
            Class<?> categoryType = Class.forName("com.zorysa.finance.categories.entity.CategoryType");
            return Enum.valueOf(categoryType.asSubclass(Enum.class), value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("CategoryType deve existir com valor " + value, exception);
        }
    }
}
