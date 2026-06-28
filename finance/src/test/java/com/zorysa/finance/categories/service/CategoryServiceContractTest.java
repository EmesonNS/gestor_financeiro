package com.zorysa.finance.categories.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class CategoryServiceContractTest {

    @Test
    void shouldListUserAndDefaultCategoriesByOptionalTypeWithPagination() {
        Method method = method("listCategories");

        assertThat(method.getReturnType().getName())
                .as("listCategories deve retornar resposta paginada sem expor entidade JPA")
                .contains("Page")
                .doesNotContain(".entity.");
        assertThat(parameterTypes(method)).contains(UUID.class, typeClass(), Pageable.class);
    }

    @Test
    void shouldCreateCategoryForAuthenticatedUserOnly() {
        Method method = method("createCategory");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.categories.dto.CategoryResponse");
        assertThat(parameterTypes(method)).contains(UUID.class, requestClass("CreateCategoryRequest"));
    }

    @Test
    void shouldCountOnlyCustomCategoriesForAuthenticatedUser() {
        Method method = method("countCustomCategories");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.categories.dto.CategoryCountResponse");
        assertThat(parameterTypes(method))
                .as("countCustomCategories deve derivar userId do usuario autenticado")
                .contains(UUID.class);
        assertThat(method.getParameterCount())
                .as("contagem nao deve aceitar filtros livres que permitam contar dados de outro usuario")
                .isEqualTo(1);
    }

    @Test
    void shouldCountApplicableIncomeAndExpenseCategoriesForAuthenticatedUser() {
        Method method = method("countCategoriesByType");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.categories.dto.CategoryTypeCountsResponse");
        assertThat(parameterTypes(method))
                .as("countCategoriesByType deve derivar userId do usuario autenticado")
                .contains(UUID.class);
        assertThat(method.getParameterCount())
                .as("contagem por tipo nao deve aceitar filtros livres que permitam contar dados de outro usuario")
                .isEqualTo(1);
    }

    @Test
    void shouldReadOnlyDefaultOrOwnedCategory() {
        Method method = method("getCategory");

        assertThat(method.getReturnType().getName()).isEqualTo("com.zorysa.finance.categories.dto.CategoryResponse");
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as("getCategory deve receber userId autenticado e categoryId")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateAndDeleteOnlyOwnedCategories() {
        assertThat(method("updateCategory")).satisfies(this::requiresAuthenticatedUserAndCategoryId);
        assertThat(method("deleteCategory")).satisfies(this::requiresAuthenticatedUserAndCategoryId);
    }

    @Test
    void shouldExposeRepositoryQueriesScopedByOwnerOrDefaultCategory() {
        Class<?> repository = findRequiredClass("com.zorysa.finance.categories.repository.CategoryRepository");

        assertThat(methodNames(repository)).contains(
                "findAllByUserIdOrDefault",
                "findByIdAndUserIdOrDefault",
                "existsByUserIdAndNameAndType",
                "countByUserIdAndDefaultCategoryFalse",
                "countApplicableByUserIdAndType"
        );
        assertThat(parameterTypes(method(repository, "findAllByUserIdOrDefault")))
                .as("consulta de colecao de categorias deve aceitar Pageable")
                .contains(Pageable.class);

        assertThat(parameterTypes(method(repository, "countByUserIdAndDefaultCategoryFalse")))
                .as("contagem de categorias personalizadas deve filtrar pelo usuario autenticado")
                .containsExactly(UUID.class);
        assertThat(method(repository, "countByUserIdAndDefaultCategoryFalse").getReturnType())
                .as("repository deve retornar quantidade numerica")
                .isIn(Long.TYPE, Long.class);

        assertThat(parameterTypes(method(repository, "countApplicableByUserIdAndType")))
                .as("contagem por tipo deve filtrar usuario autenticado e CategoryType")
                .containsExactly(UUID.class, typeClass());
        assertThat(method(repository, "countApplicableByUserIdAndType").getReturnType())
                .as("repository deve retornar quantidade numerica por tipo")
                .isIn(Long.TYPE, Long.class);
    }

    private void requiresAuthenticatedUserAndCategoryId(Method method) {
        assertThat(Arrays.stream(method.getParameterTypes()).filter(UUID.class::equals).count())
                .as(method.getName() + " deve ter dois UUIDs: userId e categoryId")
                .isGreaterThanOrEqualTo(2);
    }

    private Method method(String methodName) {
        Class<?> service = findRequiredClass("com.zorysa.finance.categories.service.CategoryService");
        return method(service, methodName);
    }

    private Method method(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getSimpleName() + " deve expor metodo " + methodName));
    }

    private Class<?> requestClass(String simpleName) {
        return findRequiredClass("com.zorysa.finance.categories.dto." + simpleName);
    }

    private Class<?> typeClass() {
        return findRequiredClass("com.zorysa.finance.categories.entity.CategoryType");
    }

    private Class<?> findRequiredClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(className + " deve existir para a etapa Categorias", exception);
        }
    }

    private Class<?>[] parameterTypes(Method method) {
        return method.getParameterTypes();
    }

    private String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(Method::getName)
                .toArray(String[]::new);
    }
}
