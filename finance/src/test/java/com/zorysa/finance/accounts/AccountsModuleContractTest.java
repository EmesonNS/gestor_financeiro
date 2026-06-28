package com.zorysa.finance.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class AccountsModuleContractTest {

    @Test
    void shouldExposeAccountsModuleClassesRequiredByFinancialAccountsFeature() {
        assertThatClassExists("com.zorysa.finance.accounts.controller.AccountController");
        assertThatClassExists("com.zorysa.finance.accounts.service.AccountService");
        assertThatClassExists("com.zorysa.finance.accounts.repository.AccountRepository");
        assertThatClassExists("com.zorysa.finance.accounts.entity.Account");
        assertThatClassExists("com.zorysa.finance.accounts.entity.AccountType");
        assertThatClassExists("com.zorysa.finance.accounts.dto.AccountResponse");
        assertThatClassExists("com.zorysa.finance.accounts.dto.CreateAccountRequest");
        assertThatClassExists("com.zorysa.finance.accounts.dto.UpdateAccountRequest");
    }

    @Test
    void shouldMapAccountControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.accounts.controller.AccountController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("AccountController deve declarar base path /api/accounts")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api/accounts");

        assertThat(mappedEndpoints(controller)).contains(
                "GET ",
                "POST ",
                "GET /{id}",
                "PUT /{id}",
                "DELETE /{id}",
                "PATCH /{id}/archive"
        );
    }

    @Test
    void shouldExposeAccountServiceOperationsForAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.accounts.service.AccountService");

        assertThat(methodNames(service)).contains(
                "listAccounts",
                "createAccount",
                "getAccount",
                "updateAccount",
                "archiveAccount",
                "deleteAccount"
        );
        assertThatMethod(method(service, "listAccounts")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "createAccount")).hasParameter(UUID.class);
        assertThatMethod(method(service, "getAccount")).hasParameter(UUID.class);
        assertThatMethod(method(service, "updateAccount")).hasParameter(UUID.class);
        assertThatMethod(method(service, "archiveAccount")).hasParameter(UUID.class);
        assertThatMethod(method(service, "deleteAccount")).hasParameter(UUID.class);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Contas financeiras", exception);
        }
    }

    private Method method(Class<?> service, String methodName) {
        return Arrays.stream(service.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(service.getSimpleName() + " deve expor metodo " + methodName));
    }

    private String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).map(Method::getName).toArray(String[]::new);
    }

    private String[] mappedEndpoints(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .flatMap(method -> {
                    GetMapping get = method.getAnnotation(GetMapping.class);
                    if (get != null) return Arrays.stream(paths(get.path(), get.value())).map(path -> "GET " + path);
                    PostMapping post = method.getAnnotation(PostMapping.class);
                    if (post != null) return Arrays.stream(paths(post.path(), post.value())).map(path -> "POST " + path);
                    PutMapping put = method.getAnnotation(PutMapping.class);
                    if (put != null) return Arrays.stream(paths(put.path(), put.value())).map(path -> "PUT " + path);
                    DeleteMapping delete = method.getAnnotation(DeleteMapping.class);
                    if (delete != null) return Arrays.stream(paths(delete.path(), delete.value())).map(path -> "DELETE " + path);
                    PatchMapping patch = method.getAnnotation(PatchMapping.class);
                    if (patch != null) return Arrays.stream(paths(patch.path(), patch.value())).map(path -> "PATCH " + path);
                    return Arrays.<String>stream(new String[0]);
                })
                .toArray(String[]::new);
    }

    private String[] paths(String[] path, String[] value) {
        String[] mappings = path.length > 0 ? path : value;
        return mappings.length == 0 ? new String[]{""} : mappings;
    }

    private MethodContract assertThatMethod(Method method) {
        return new MethodContract(method);
    }

    private static class MethodContract {
        private final Method method;

        MethodContract(Method method) {
            this.method = method;
        }

        MethodContract hasParameter(Class<?> type) {
            assertThat(Arrays.asList(method.getParameterTypes()))
                    .as(method.getName() + " deve receber parametro " + type.getSimpleName())
                    .contains(type);
            return this;
        }
    }
}
