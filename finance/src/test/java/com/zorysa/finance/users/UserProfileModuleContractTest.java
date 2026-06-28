package com.zorysa.finance.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class UserProfileModuleContractTest {

    @Test
    void shouldExposeControllerAndDtosRequiredByProfileFeature() {
        assertThatClassExists("com.zorysa.finance.users.controller.UserProfileController");
        assertThatClassExists("com.zorysa.finance.users.dto.UpdateUserProfileRequest");
        assertThatClassExists("com.zorysa.finance.users.dto.ChangePasswordRequest");
    }

    @Test
    void shouldMapUserProfileControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.users.controller.UserProfileController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("UserProfileController deve declarar base path /api/users")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api/users");

        assertThat(mappedEndpoints(controller)).contains(
                "GET /me",
                "PUT /me",
                "PUT /me/password"
        );
    }

    @Test
    void shouldExposeUserServiceOperationsForAuthenticatedProfile() {
        Class<?> service = findRequiredClass("com.zorysa.finance.users.service.UserService");

        assertThat(methodNames(service)).contains(
                "getAuthenticatedProfile",
                "updateAuthenticatedProfile",
                "changeAuthenticatedPassword"
        );
        assertThatMethod(method("getAuthenticatedProfile")).hasParameter(UUID.class);
        assertThatMethod(method("updateAuthenticatedProfile")).hasParameter(UUID.class);
        assertThatMethod(method("changeAuthenticatedPassword")).hasParameter(UUID.class);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Perfil do usuario", exception);
        }
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.users.service.UserService");
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

    private String[] mappedEndpoints(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .flatMap(method -> {
                    GetMapping get = method.getAnnotation(GetMapping.class);
                    if (get != null) {
                        return Arrays.stream(paths(get.path(), get.value())).map(path -> "GET " + path);
                    }
                    PutMapping put = method.getAnnotation(PutMapping.class);
                    if (put != null) {
                        return Arrays.stream(paths(put.path(), put.value())).map(path -> "PUT " + path);
                    }
                    return Arrays.<String>stream(new String[0]);
                })
                .toArray(String[]::new);
    }

    private String[] paths(String[] path, String[] value) {
        String[] mappings = path.length > 0 ? path : value;
        return mappings.length == 0 ? new String[]{""} : mappings;
    }

    private static class MethodContract {

        private final Method method;

        MethodContract(Method method) {
            this.method = method;
        }

        void hasParameter(Class<?> type) {
            assertThat(Arrays.asList(method.getParameterTypes()))
                    .as(method.getName() + " deve receber parametro " + type.getSimpleName())
                    .contains(type);
        }
    }

    private MethodContract assertThatMethod(Method method) {
        return new MethodContract(method);
    }
}
