package com.zorysa.finance.installments.dto;

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

class InstallmentRequestValidationTest {

    private static final UUID CATEGORY_ID = UUID.fromString("27272727-2727-2727-2727-272727272727");

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
    void shouldRejectCreatePurchaseWithoutRequiredFields() {
        Object request = instantiateRecord(
                "com.zorysa.finance.installments.dto.CreateCardPurchaseRequest",
                "",
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("description", "categoryId", "totalAmount", "purchaseDate", "installmentCount");
    }

    @Test
    void shouldRejectCreatePurchaseWithNonPositiveAmountAndInstallments() {
        Object request = instantiateRecord(
                "com.zorysa.finance.installments.dto.CreateCardPurchaseRequest",
                "Notebook",
                CATEGORY_ID,
                BigDecimal.ZERO,
                LocalDate.of(2026, 6, 20),
                0,
                ""
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("totalAmount", "installmentCount");
    }

    @Test
    void shouldAcceptSingleInstallmentPurchase() {
        Object request = instantiateRecord(
                "com.zorysa.finance.installments.dto.CreateCardPurchaseRequest",
                "Mercado",
                CATEGORY_ID,
                new BigDecimal("250.00"),
                LocalDate.of(2026, 6, 20),
                1,
                null
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldRejectUpdatePurchaseWithInvalidAmountAndInstallments() {
        Object request = instantiateRecord(
                "com.zorysa.finance.installments.dto.UpdateCardPurchaseRequest",
                "Notebook",
                CATEGORY_ID,
                new BigDecimal("-1.00"),
                LocalDate.of(2026, 6, 20),
                0,
                "Atualizado"
        );

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("totalAmount", "installmentCount");
    }

    @Test
    void shouldExposePurchaseAndInstallmentResponses() {
        assertRecordComponents(
                "com.zorysa.finance.installments.dto.CardPurchaseResponse",
                "id", "creditCardId", "categoryId", "description", "totalAmount", "purchaseDate",
                "installmentCount", "status", "notes", "installments", "createdAt", "updatedAt"
        );
        assertRecordComponents(
                "com.zorysa.finance.installments.dto.InstallmentResponse",
                "id", "purchaseId", "invoiceId", "installmentNumber", "totalInstallments", "amount",
                "competenceMonth", "competenceYear", "status", "createdAt", "updatedAt"
        );
    }

    private Object instantiateRecord(String className, Object... values) {
        try {
            Class<?> type = Class.forName(className);
            RecordComponent[] components = type.getRecordComponents();
            assertThat(components).as(className + " deve ser record").hasSize(values.length);
            Class<?>[] parameterTypes = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
            return type.getDeclaredConstructor(parameterTypes).newInstance(values);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(className + " deve existir com componentes documentados", exception);
        }
    }

    private void assertRecordComponents(String className, String... componentNames) {
        try {
            Class<?> type = Class.forName(className);
            assertThat(type.getRecordComponents()).extracting(RecordComponent::getName).contains(componentNames);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para compras parceladas", exception);
        }
    }
}
