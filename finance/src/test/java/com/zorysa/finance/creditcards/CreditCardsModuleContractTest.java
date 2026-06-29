package com.zorysa.finance.creditcards;

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

class CreditCardsModuleContractTest {

    @Test
    void shouldExposeCreditCardsModuleClassesRequiredByCreditCardsFeature() {
        assertThatClassExists("com.zorysa.finance.creditcards.controller.CreditCardController");
        assertThatClassExists("com.zorysa.finance.creditcards.service.CreditCardService");
        assertThatClassExists("com.zorysa.finance.creditcards.repository.CreditCardRepository");
        assertThatClassExists("com.zorysa.finance.creditcards.entity.CreditCard");
        assertThatClassExists("com.zorysa.finance.creditcards.dto.CreditCardResponse");
        assertThatClassExists("com.zorysa.finance.creditcards.dto.CreateCreditCardRequest");
        assertThatClassExists("com.zorysa.finance.creditcards.dto.UpdateCreditCardRequest");
    }

    @Test
    void shouldMapCreditCardControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.creditcards.controller.CreditCardController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("CreditCardController deve declarar base path /api/credit-cards")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api/credit-cards");

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
    void shouldExposeCreditCardServiceOperationsForAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.creditcards.service.CreditCardService");

        assertThat(methodNames(service)).contains(
                "listCreditCards",
                "createCreditCard",
                "getCreditCard",
                "updateCreditCard",
                "deleteCreditCard",
                "archiveCreditCard"
        );
        assertThatMethod(method(service, "listCreditCards")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "createCreditCard")).hasParameter(UUID.class);
        assertThatMethod(method(service, "getCreditCard")).hasParameter(UUID.class);
        assertThatMethod(method(service, "updateCreditCard")).hasParameter(UUID.class);
        assertThatMethod(method(service, "deleteCreditCard")).hasParameter(UUID.class);
        assertThatMethod(method(service, "archiveCreditCard")).hasParameter(UUID.class);
    }

    @Test
    void shouldRequirePaginationOnlyOnCreditCardCollectionEndpoint() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.creditcards.controller.CreditCardController");

        Method listMethod = mappedMethod(controller, "");
        Method getByIdMethod = mappedMethod(controller, "/{id}");

        assertThat(Arrays.asList(listMethod.getParameterTypes()))
                .as("GET /api/credit-cards deve aceitar page, size e sort via Pageable")
                .contains(Pageable.class);
        assertThat(Arrays.asList(getByIdMethod.getParameterTypes()))
                .as("GET /api/credit-cards/{id} retorna objeto unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Cartoes de credito", exception);
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

    private Method mappedMethod(Class<?> controller, String path) {
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(GetMapping.class))
                .filter(method -> Arrays.asList(paths(
                        method.getAnnotation(GetMapping.class).path(),
                        method.getAnnotation(GetMapping.class).value()
                )).contains(path))
                .findFirst()
                .orElseThrow(() -> new AssertionError("CreditCardController deve mapear GET " + path));
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
