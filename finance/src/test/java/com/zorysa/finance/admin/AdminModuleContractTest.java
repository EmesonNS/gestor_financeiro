package com.zorysa.finance.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.junit.jupiter.api.Test;

class AdminModuleContractTest {

    @Test
    void shouldExposeAdminModuleClassesRequiredByUserApprovalFlow() {
        assertThatClassExists("com.zorysa.finance.admin.controller.AdminUserController");
        assertThatClassExists("com.zorysa.finance.admin.service.AdminUserService");
        assertThatClassExists("com.zorysa.finance.admin.dto.AdminUserResponse");
        assertThatClassExists("com.zorysa.finance.admin.dto.AdminUserDetailsResponse");
        assertThatClassExists("com.zorysa.finance.admin.dto.AdminStatusChangeRequest");
        assertThatClassExists("com.zorysa.finance.admin.dto.UserStatusHistoryResponse");
        assertThatClassExists("com.zorysa.finance.admin.entity.UserStatusHistory");
        assertThatClassExists("com.zorysa.finance.admin.repository.UserStatusHistoryRepository");
    }

    @Test
    void shouldExposeServiceOperationsForEveryAdministrativeUserAction() {
        Class<?> service = findRequiredClass("com.zorysa.finance.admin.service.AdminUserService");

        assertThat(methodNames(service)).contains(
                "listUsers",
                "listPendingUsers",
                "getUserDetails",
                "approveUser",
                "rejectUser",
                "suspendUser",
                "reactivateUser",
                "deleteUser"
        );
    }

    @Test
    void shouldMapAdminControllerToDocumentedUserAdministrationEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.admin.controller.AdminUserController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("AdminUserController deve declarar base path /api/admin/users")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api/admin/users");

        assertThat(mappedPaths(controller)).contains(
                "GET ",
                "GET /pending",
                "GET /{userId}",
                "PATCH /{userId}/approve",
                "PATCH /{userId}/reject",
                "PATCH /{userId}/suspend",
                "PATCH /{userId}/reactivate",
                "DELETE /{userId}"
        );
    }

    private String[] mappedPaths(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .flatMap(this::mappedPaths)
                .toArray(String[]::new);
    }

    private Stream<String> mappedPaths(Method method) {
        GetMapping get = method.getAnnotation(GetMapping.class);
        if (get != null) {
            return paths(get.path(), get.value()).map(path -> "GET " + path);
        }
        PatchMapping patch = method.getAnnotation(PatchMapping.class);
        if (patch != null) {
            return paths(patch.path(), patch.value()).map(path -> "PATCH " + path);
        }
        DeleteMapping delete = method.getAnnotation(DeleteMapping.class);
        if (delete != null) {
            return paths(delete.path(), delete.value()).map(path -> "DELETE " + path);
        }
        return Stream.empty();
    }

    private Stream<String> paths(String[] path, String[] value) {
        String[] mappings = path.length > 0 ? path : value;
        if (mappings.length == 0) {
            return Stream.of("");
        }
        return Arrays.stream(mappings);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa de administracao de usuarios", exception);
        }
    }

    private String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(method -> method.getName())
                .toArray(String[]::new);
    }
}
