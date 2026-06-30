package com.zorysa.finance.invoices;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class InvoicesModuleContractTest {

    @Test
    void shouldExposeInvoicesModuleClassesRequiredByInvoicesFeature() {
        assertThatClassExists("com.zorysa.finance.invoices.controller.InvoiceController");
        assertThatClassExists("com.zorysa.finance.invoices.service.InvoiceService");
        assertThatClassExists("com.zorysa.finance.invoices.repository.InvoiceRepository");
        assertThatClassExists("com.zorysa.finance.invoices.entity.CreditCardInvoice");
        assertThatClassExists("com.zorysa.finance.invoices.entity.InvoiceStatus");
        assertThatClassExists("com.zorysa.finance.invoices.dto.InvoiceResponse");
        assertThatClassExists("com.zorysa.finance.invoices.dto.PayInvoiceRequest");
    }

    @Test
    void shouldMapInvoiceControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.invoices.controller.InvoiceController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("InvoiceController deve declarar base path /api")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api");

        assertThat(mappedEndpoints(controller)).contains(
                "GET /credit-cards/{cardId}/invoices",
                "GET /credit-cards/{cardId}/invoices/current",
                "GET /invoices/{invoiceId}",
                "PATCH /invoices/{invoiceId}/pay"
        );
    }

    @Test
    void shouldExposeInvoiceServiceOperationsForAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.invoices.service.InvoiceService");

        assertThat(methodNames(service)).contains(
                "listInvoices",
                "getCurrentInvoice",
                "getInvoice",
                "payInvoice",
                "getOrCreateInvoice",
                "calculateTotalAmount"
        );
        assertThatMethod(method(service, "listInvoices")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "getCurrentInvoice")).hasParameter(UUID.class);
        assertThatMethod(method(service, "getInvoice")).hasParameter(UUID.class);
        assertThatMethod(method(service, "payInvoice")).hasParameter(UUID.class);
        assertThatMethod(method(service, "getOrCreateInvoice")).hasParameter(UUID.class);
    }

    @Test
    void shouldRequirePaginationOnlyOnInvoiceCollectionEndpoint() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.invoices.controller.InvoiceController");

        Method listMethod = mappedMethod(controller, "/credit-cards/{cardId}/invoices");
        Method getByIdMethod = mappedMethod(controller, "/invoices/{invoiceId}");
        Method currentMethod = mappedMethod(controller, "/credit-cards/{cardId}/invoices/current");

        assertThat(Arrays.asList(listMethod.getParameterTypes()))
                .as("GET /api/credit-cards/{cardId}/invoices deve aceitar page, size e sort via Pageable")
                .contains(Pageable.class);
        assertThat(Arrays.asList(getByIdMethod.getParameterTypes()))
                .as("GET /api/invoices/{invoiceId} retorna objeto unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
        assertThat(Arrays.asList(currentMethod.getParameterTypes()))
                .as("GET /api/credit-cards/{cardId}/invoices/current retorna objeto unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Faturas de cartao", exception);
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
                .orElseThrow(() -> new AssertionError("InvoiceController deve mapear GET " + path));
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
