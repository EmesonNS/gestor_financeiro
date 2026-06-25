package com.zorysa.finance.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AuthRequestValidationTest {

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
    void shouldRejectRegistrationWithoutValidNameEmailAndPassword() {
        RegisterRequest request = new RegisterRequest("", "not-an-email", "");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "email", "password");
    }

    @Test
    void shouldAcceptRegistrationWithNameValidEmailAndPassword() {
        RegisterRequest request = new RegisterRequest("Maria", "maria@email.com", "secret123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectLoginWithoutValidEmailAndPassword() {
        LoginRequest request = new LoginRequest("invalid", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "password");
    }

    @Test
    void shouldRejectRefreshWithoutToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("");

        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("refreshToken");
    }

    @Test
    void shouldRejectForgotPasswordWithoutValidEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("invalid");

        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email");
    }

    @Test
    void shouldRejectResetPasswordWithoutTokenAndNewPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest("", "");

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("token", "newPassword");
    }
}
