package com.zorysa.finance.installments;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class InstallmentsModuleContractTest {

    @Test
    void shouldExposeInstallmentsModuleClassesRequiredByInstallmentsFeature() {
        assertThatClassExists("com.zorysa.finance.installments.controller.CardPurchaseController");
        assertThatClassExists("com.zorysa.finance.installments.controller.InstallmentController");
        assertThatClassExists("com.zorysa.finance.installments.service.InstallmentService");
        assertThatClassExists("com.zorysa.finance.installments.repository.CardPurchaseRepository");
        assertThatClassExists("com.zorysa.finance.installments.repository.CreditCardInstallmentRepository");
        assertThatClassExists("com.zorysa.finance.installments.entity.CardPurchase");
        assertThatClassExists("com.zorysa.finance.installments.entity.CreditCardInstallment");
        assertThatClassExists("com.zorysa.finance.installments.entity.PurchaseStatus");
        assertThatClassExists("com.zorysa.finance.installments.entity.InstallmentStatus");
        assertThatClassExists("com.zorysa.finance.installments.dto.CardPurchaseResponse");
        assertThatClassExists("com.zorysa.finance.installments.dto.InstallmentResponse");
        assertThatClassExists("com.zorysa.finance.installments.dto.CreateCardPurchaseRequest");
        assertThatClassExists("com.zorysa.finance.installments.dto.UpdateCardPurchaseRequest");
    }

    @Test
    void shouldMapCardPurchaseControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.installments.controller.CardPurchaseController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping).isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api");

        assertThat(mappedEndpoints(controller)).contains(
                "POST /credit-cards/{cardId}/purchases",
                "GET /credit-cards/{cardId}/purchases",
                "GET /card-purchases/{purchaseId}",
                "PUT /card-purchases/{purchaseId}",
                "DELETE /card-purchases/{purchaseId}"
        );
    }

    @Test
    void shouldMapInstallmentControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.installments.controller.InstallmentController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping).isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api");

        assertThat(mappedEndpoints(controller)).contains(
                "GET /installments",
                "GET /installments/future",
                "GET /card-purchases/{purchaseId}/installments"
        );
    }

    @Test
    void shouldExposeInstallmentServiceOperationsForAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.installments.service.InstallmentService");

        assertThat(methodNames(service)).contains(
                "createPurchase",
                "listPurchases",
                "getPurchase",
                "updatePurchase",
                "deletePurchase",
                "listInstallments",
                "listFutureInstallments",
                "listPurchaseInstallments",
                "generateInstallments"
        );
        assertThatMethod(method(service, "createPurchase")).hasParameter(UUID.class);
        assertThatMethod(method(service, "listPurchases")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "listInstallments")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "listFutureInstallments")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "listPurchaseInstallments")).hasParameter(UUID.class).hasParameter(Pageable.class);
    }

    @Test
    void shouldRequirePaginationOnCollectionEndpoints() {
        Class<?> purchaseController = findRequiredClass("com.zorysa.finance.installments.controller.CardPurchaseController");
        Class<?> installmentController = findRequiredClass("com.zorysa.finance.installments.controller.InstallmentController");

        assertThat(Arrays.asList(mappedMethod(purchaseController, "/credit-cards/{cardId}/purchases").getParameterTypes()))
                .contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(installmentController, "/installments").getParameterTypes()))
                .contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(installmentController, "/installments/future").getParameterTypes()))
                .contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(installmentController, "/card-purchases/{purchaseId}/installments").getParameterTypes()))
                .contains(Pageable.class);
        assertThat(Arrays.asList(mappedMethod(purchaseController, "/card-purchases/{purchaseId}").getParameterTypes()))
                .doesNotContain(Pageable.class);
    }

    @Test
    void shouldExposeFutureInstallmentsRangeQueryParameters() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.installments.controller.InstallmentController");
        Method method = mappedMethod(controller, "/installments/future");

        assertThat(requestParamNames(method)).contains("cardId", "fromMonth", "fromYear", "toMonth", "toYear");
        assertThat(Arrays.stream(method.getParameterTypes()).filter(Integer.class::equals).count())
                .as("GET /api/installments/future deve aceitar fromMonth, fromYear, toMonth e toYear")
                .isGreaterThanOrEqualTo(4);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Compras parceladas", exception);
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
                .orElseThrow(() -> new AssertionError(controller.getSimpleName() + " deve mapear GET " + path));
    }

    private MethodContract assertThatMethod(Method method) {
        return new MethodContract(method);
    }

    private String[] requestParamNames(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(org.springframework.web.bind.annotation.RequestParam.class))
                .map(parameter -> {
                    org.springframework.web.bind.annotation.RequestParam annotation =
                            parameter.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
                    if (!annotation.name().isBlank()) return annotation.name();
                    if (!annotation.value().isBlank()) return annotation.value();
                    return parameter.getName();
                })
                .toArray(String[]::new);
    }

    private static class MethodContract {
        private final Method method;

        MethodContract(Method method) {
            this.method = method;
        }

        MethodContract hasParameter(Class<?> type) {
            assertThat(Arrays.asList(method.getParameterTypes())).contains(type);
            return this;
        }
    }
}
