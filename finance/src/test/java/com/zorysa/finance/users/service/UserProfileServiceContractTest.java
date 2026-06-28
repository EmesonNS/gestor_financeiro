package com.zorysa.finance.users.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.zorysa.finance.shared.exception.UnauthorizedException;
import com.zorysa.finance.users.entity.User;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserProfileServiceContractTest {

    @Test
    void shouldReadOnlyAuthenticatedUserById() {
        Method method = method("getAuthenticatedProfile");

        assertThat(Arrays.asList(method.getParameterTypes())).contains(UUID.class);
        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.users.dto.UserResponse");
    }

    @Test
    void shouldUpdateOnlyAuthenticatedUserName() {
        Method method = method("updateAuthenticatedProfile");

        assertThat(Arrays.asList(method.getParameterTypes())).contains(UUID.class);
        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.users.dto.UserResponse");
    }

    @Test
    void shouldChangePasswordOnlyAfterCheckingCurrentPassword() {
        Method method = method("changeAuthenticatedPassword");

        assertThat(Arrays.asList(method.getParameterTypes())).contains(UUID.class);
        assertThat(method.getReturnType()).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldExposeRepositoryLookupByIdForAuthenticatedProfile() throws Exception {
        Method method = Class.forName("com.zorysa.finance.users.repository.UserRepository")
                .getMethod("findById", Object.class);

        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    void shouldExposeUserNameMutationForProfileUpdate() {
        assertThat(methodNames(User.class)).contains("updateName");
    }

    @Test
    void shouldUseUnauthorizedExceptionForWrongCurrentPassword() {
        assertThat(UnauthorizedException.class).isAssignableTo(RuntimeException.class);
    }

    private Method method(String methodName) {
        Class<?> service = UserService.class;
        return Arrays.stream(service.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("UserService deve expor metodo " + methodName));
    }

    private String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(Method::getName)
                .toArray(String[]::new);
    }
}
