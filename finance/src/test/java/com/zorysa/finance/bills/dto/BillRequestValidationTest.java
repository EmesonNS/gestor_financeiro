package com.zorysa.finance.bills.dto;

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

class BillRequestValidationTest {

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
    void shouldRejectCreateBillWithoutRequiredFields() {
        Object request = instantiateRecord(
                "com.zorysa.finance.bills.dto.CreateBillRequest",
                "",
                BigDecimal.ZERO,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("description", "amount", "dueDate", "categoryId", "status");
    }

    @Test
    void shouldAcceptPendingBillWithoutAccountUntilPayment() {
        Object request = instantiateRecord(
                "com.zorysa.finance.bills.dto.CreateBillRequest",
                "Energia",
                new BigDecimal("180.00"),
                LocalDate.of(2026, 6, 25),
                CATEGORY_ID,
                null,
                enumValue("PENDING")
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectUpdateBillWithNegativeAmount() {
        Object request = instantiateRecord(
                "com.zorysa.finance.bills.dto.UpdateBillRequest",
                "Energia",
                new BigDecimal("-1.00"),
                LocalDate.of(2026, 6, 25),
                CATEGORY_ID,
                ACCOUNT_ID,
                enumValue("PENDING")
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("amount");
    }

    @Test
    void shouldRejectPayBillWithoutAccountAndPaymentDate() {
        Object request = instantiateRecord(
                "com.zorysa.finance.bills.dto.PayBillRequest",
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("accountId", "paidAt");
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
            Class<?> enumClass = Class.forName("com.zorysa.finance.bills.entity.BillStatus");
            return Enum.valueOf(enumClass.asSubclass(Enum.class), value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("BillStatus deve existir com valor " + value, exception);
        }
    }
}
