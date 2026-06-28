package com.zorysa.finance.users.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserProfileRequestValidationTest {

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
    void shouldRejectProfileUpdateWithoutName() {
        Object request = instantiate("com.zorysa.finance.users.dto.UpdateUserProfileRequest", "");

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void shouldRejectPasswordChangeWithoutCurrentAndNewPassword() {
        Object request = instantiate("com.zorysa.finance.users.dto.ChangePasswordRequest", "", "");

        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("currentPassword", "newPassword");
    }

    private Object instantiate(String className, Object... args) {
        try {
            Class<?> type = Class.forName(className);
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int index = 0; index < args.length; index++) {
                parameterTypes[index] = String.class;
            }
            return type.getDeclaredConstructor(parameterTypes).newInstance(args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(className + " deve existir com construtor de strings para validar requests de perfil", exception);
        }
    }
}
