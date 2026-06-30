package com.zorysa.finance.invoices.dto;

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
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InvoiceRequestValidationTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("20202020-2020-2020-2020-202020202020");

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
    void shouldRejectPayInvoiceWithoutAccountAndPaymentDate() {
        Object request = instantiateRecord(
                "com.zorysa.finance.invoices.dto.PayInvoiceRequest",
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("paymentAccountId", "paidAt");
    }

    @Test
    void shouldAcceptPayInvoiceWithAccountAndPaymentDate() {
        Object request = instantiateRecord(
                "com.zorysa.finance.invoices.dto.PayInvoiceRequest",
                ACCOUNT_ID,
                LocalDate.of(2026, 6, 20)
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldExposeInvoiceResponseWithPaymentAndTotalComponents() {
        try {
            Class<?> type = Class.forName("com.zorysa.finance.invoices.dto.InvoiceResponse");
            RecordComponent[] components = type.getRecordComponents();

            assertThat(components)
                    .as("InvoiceResponse deve ser record para expor fatura, total e pagamento")
                    .isNotNull();
            assertThat(components).extracting(RecordComponent::getName)
                    .contains(
                            "id",
                            "creditCardId",
                            "referenceMonth",
                            "referenceYear",
                            "closingDate",
                            "dueDate",
                            "totalAmount",
                            "status",
                            "paidAt",
                            "paymentAccountId",
                            "createdAt",
                            "updatedAt"
                    );
            assertThat(componentType(type, "totalAmount")).isEqualTo(BigDecimal.class);
            assertThat(componentType(type, "closingDate")).isEqualTo(LocalDate.class);
            assertThat(componentType(type, "dueDate")).isEqualTo(LocalDate.class);
            assertThat(componentType(type, "paidAt")).isEqualTo(LocalDate.class);
            assertThat(componentType(type, "status").getName()).isEqualTo("com.zorysa.finance.invoices.entity.InvoiceStatus");
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("InvoiceResponse deve existir para faturas de cartao", exception);
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
