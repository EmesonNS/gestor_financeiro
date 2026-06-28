package com.zorysa.finance.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class AdminUserServiceContractTest {

    @Test
    void shouldExposeReadOperationsForAdministrativeUserManagement() {
        Class<?> service = findRequiredClass("com.zorysa.finance.admin.service.AdminUserService");

        assertThat(methodNames(service)).contains(
                "listUsers",
                "listPendingUsers",
                "getUserDetails"
        );
        assertThatMethod(method("listUsers")).hasParameterAssignableTo(Pageable.class);
        assertThatMethod(method("listPendingUsers")).hasParameterAssignableTo(Pageable.class);
        assertThatMethod(method("getUserDetails")).hasParameterAssignableTo(UUID.class);
    }

    @Test
    void shouldExposeApprovalAndReactivationOperationsWithAdminActorAndOptionalReason() {
        assertStatusChangeMethod("approveUser");
        assertStatusChangeMethod("reactivateUser");
    }

    @Test
    void shouldExposeRejectSuspendAndDeleteOperationsWithAdminActorAndReason() {
        assertStatusChangeMethod("rejectUser");
        assertStatusChangeMethod("suspendUser");
        assertStatusChangeMethod("deleteUser");
    }

    @Test
    void shouldExposeAuditHistoryContractForAdministrativeDecisions() {
        Class<?> history = findRequiredClass("com.zorysa.finance.admin.entity.UserStatusHistory");

        assertThat(fieldNames(history)).contains(
                "user",
                "adminUser",
                "previousStatus",
                "newStatus",
                "action",
                "reason",
                "createdAt"
        );
    }

    private void assertStatusChangeMethod(String methodName) {
        Method method = method(methodName);

        assertThat(method.getParameterTypes())
                .as(methodName + " deve receber userId e adminUserId")
                .contains(UUID.class, UUID.class);
        assertThatMethod(method).hasParameterAssignableTo(String.class);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.admin.service.AdminUserService");
        return Arrays.stream(service.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("AdminUserService deve expor metodo " + methodName));
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para testar regras de service da administracao", exception);
        }
    }

    private String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(Method::getName)
                .toArray(String[]::new);
    }

    private String[] fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(field -> field.getName())
                .toArray(String[]::new);
    }

    private static class MethodAssert {

        private final Method method;

        MethodAssert(Method method) {
            this.method = method;
        }

        MethodAssert hasParameterAssignableTo(Class<?> expectedType) {
            boolean hasParameter = Arrays.stream(method.getParameters())
                    .map(Parameter::getType)
                    .anyMatch(expectedType::isAssignableFrom);
            assertThat(hasParameter)
                    .as(method.getName() + " deve receber parametro atribuivel a " + expectedType.getSimpleName())
                    .isTrue();
            return this;
        }
    }

    private MethodAssert assertThatMethod(Method method) {
        return new MethodAssert(method);
    }
}
