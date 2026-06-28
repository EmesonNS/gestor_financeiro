package com.zorysa.finance.categories;

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

class CategoriesModuleContractTest {

    @Test
    void shouldExposeCategoriesModuleClassesRequiredByCategoriesFeature() {
        assertThatClassExists("com.zorysa.finance.categories.controller.CategoryController");
        assertThatClassExists("com.zorysa.finance.categories.service.CategoryService");
        assertThatClassExists("com.zorysa.finance.categories.repository.CategoryRepository");
        assertThatClassExists("com.zorysa.finance.categories.entity.Category");
        assertThatClassExists("com.zorysa.finance.categories.entity.CategoryType");
        assertThatClassExists("com.zorysa.finance.categories.dto.CategoryResponse");
        assertThatClassExists("com.zorysa.finance.categories.dto.CreateCategoryRequest");
        assertThatClassExists("com.zorysa.finance.categories.dto.UpdateCategoryRequest");
        assertThatClassExists("com.zorysa.finance.categories.dto.CategoryCountResponse");
        assertThatClassExists("com.zorysa.finance.categories.dto.CategoryTypeCountsResponse");
    }

    @Test
    void shouldMapCategoryControllerToDocumentedEndpoints() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.categories.controller.CategoryController");

        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        assertThat(classMapping)
                .as("CategoryController deve declarar base path /api/categories")
                .isNotNull();
        assertThat(paths(classMapping.path(), classMapping.value())).contains("/api/categories");

        assertThat(mappedEndpoints(controller)).contains(
                "GET ",
                "POST ",
                "GET /custom/count",
                "GET /type-counts",
                "GET /{id}",
                "PUT /{id}",
                "DELETE /{id}"
        );
    }

    @Test
    void shouldExposeCategoryServiceOperationsForAuthenticatedUser() {
        Class<?> service = findRequiredClass("com.zorysa.finance.categories.service.CategoryService");

        assertThat(methodNames(service)).contains(
                "listCategories",
                "createCategory",
                "getCategory",
                "countCustomCategories",
                "countCategoriesByType",
                "updateCategory",
                "deleteCategory"
        );
        assertThatMethod(method(service, "listCategories")).hasParameter(UUID.class).hasParameter(Pageable.class);
        assertThatMethod(method(service, "createCategory")).hasParameter(UUID.class);
        assertThatMethod(method(service, "getCategory")).hasParameter(UUID.class);
        assertThatMethod(method(service, "countCustomCategories")).hasParameter(UUID.class);
        assertThatMethod(method(service, "countCategoriesByType")).hasParameter(UUID.class);
        assertThatMethod(method(service, "updateCategory")).hasParameter(UUID.class);
        assertThatMethod(method(service, "deleteCategory")).hasParameter(UUID.class);
    }

    @Test
    void shouldRequirePaginationOnlyOnCategoryCollectionEndpoint() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.categories.controller.CategoryController");

        Method listMethod = mappedMethod(controller, "");
        Method getByIdMethod = mappedMethod(controller, "/{id}");

        assertThat(Arrays.asList(listMethod.getParameterTypes()))
                .as("GET /api/categories deve aceitar page, size e sort via Pageable")
                .contains(Pageable.class);
        assertThat(Arrays.asList(getByIdMethod.getParameterTypes()))
                .as("GET /api/categories/{id} retorna objeto unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
    }

    @Test
    void shouldExposeNonPaginatedCustomCategoryCountEndpoint() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.categories.controller.CategoryController");

        Method countMethod = mappedMethod(controller, "/custom/count");

        assertThat(Arrays.asList(countMethod.getParameterTypes()))
                .as("GET /api/categories/custom/count retorna resumo unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
        assertThat(countMethod.getReturnType().getName())
                .as("endpoint de contagem deve retornar DTO proprio, sem expor entidade")
                .isEqualTo("com.zorysa.finance.categories.dto.CategoryCountResponse");
    }

    @Test
    void shouldExposeNonPaginatedCategoryTypeCountsEndpoint() {
        Class<?> controller = findRequiredClass("com.zorysa.finance.categories.controller.CategoryController");

        Method countMethod = mappedMethod(controller, "/type-counts");

        assertThat(Arrays.asList(countMethod.getParameterTypes()))
                .as("GET /api/categories/type-counts retorna resumo unico e nao deve ser paginado")
                .doesNotContain(Pageable.class);
        assertThat(countMethod.getReturnType().getName())
                .as("endpoint de contagem por tipo deve retornar DTO proprio, sem expor entidade")
                .isEqualTo("com.zorysa.finance.categories.dto.CategoryTypeCountsResponse");
    }

    private void assertThatClassExists(String className) {
        findRequiredClass(className);
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Categorias", exception);
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
                .orElseThrow(() -> new AssertionError("CategoryController deve mapear GET " + path));
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
