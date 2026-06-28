package com.zorysa.finance.categories.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryDomainContractTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Test
    void shouldCreatePersonalCategoryForAuthenticatedUser() {
        Object type = enumValue("EXPENSE");
        Object category = instantiateCategory(OWNER_ID, "Alimentacao", type, "#16a34a", "utensils", false);

        assertThat(invoke(category, "getUserId")).isEqualTo(OWNER_ID);
        assertThat(invoke(category, "getName")).isEqualTo("Alimentacao");
        assertThat(invoke(category, "getType")).isEqualTo(type);
        assertThat(invoke(category, "getColor")).isEqualTo("#16a34a");
        assertThat(invoke(category, "getIcon")).isEqualTo("utensils");
        assertThat(invoke(category, "isDefault")).isEqualTo(false);
    }

    @Test
    void shouldAllowGlobalDefaultCategoryWithoutOwner() {
        Object category = instantiateCategory(null, "Salario", enumValue("INCOME"), null, "wallet", true);

        assertThat(invoke(category, "getUserId")).isNull();
        assertThat(invoke(category, "isDefault")).isEqualTo(true);
        assertThat(invoke(category, "isUsableBy", OWNER_ID)).isEqualTo(true);
        assertThat(invoke(category, "isUsableBy", OTHER_USER_ID)).isEqualTo(true);
    }

    @Test
    void shouldRestrictPersonalCategoryToItsOwner() {
        Object category = instantiateCategory(OWNER_ID, "Freelance", enumValue("INCOME"), "#0ea5e9", "briefcase", false);

        assertThat(invoke(category, "belongsTo", OWNER_ID)).isEqualTo(true);
        assertThat(invoke(category, "belongsTo", OTHER_USER_ID)).isEqualTo(false);
        assertThat(invoke(category, "isUsableBy", OWNER_ID)).isEqualTo(true);
        assertThat(invoke(category, "isUsableBy", OTHER_USER_ID)).isEqualTo(false);
    }

    @Test
    void shouldUpdateEditablePersonalCategoryDetails() {
        Object category = instantiateCategory(OWNER_ID, "Mercado", enumValue("EXPENSE"), "#16a34a", "cart", false);

        invoke(category, "updateDetails", "Supermercado", enumValue("EXPENSE"), "#15803d", "shopping-cart");

        assertThat(invoke(category, "getUserId")).isEqualTo(OWNER_ID);
        assertThat(invoke(category, "getName")).isEqualTo("Supermercado");
        assertThat(invoke(category, "getType")).isEqualTo(enumValue("EXPENSE"));
        assertThat(invoke(category, "getColor")).isEqualTo("#15803d");
        assertThat(invoke(category, "getIcon")).isEqualTo("shopping-cart");
    }

    private Object instantiateCategory(UUID userId, String name, Object type, String color, String icon, boolean defaultCategory) {
        try {
            Class<?> categoryClass = Class.forName("com.zorysa.finance.categories.entity.Category");
            return categoryClass
                    .getDeclaredConstructor(UUID.class, String.class, type.getClass(), String.class, String.class, boolean.class)
                    .newInstance(userId, name, type, color, icon, defaultCategory);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Category deve expor construtor (UUID userId, String name, CategoryType type, String color, String icon, boolean defaultCategory)",
                    exception
            );
        }
    }

    private Object enumValue(String value) {
        try {
            Class<?> categoryType = Class.forName("com.zorysa.finance.categories.entity.CategoryType");
            return Enum.valueOf(categoryType.asSubclass(Enum.class), value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("CategoryType deve existir com valor " + value, exception);
        }
    }

    private Object invoke(Object target, String methodName, Object... args) {
        Method method = Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .filter(candidate -> candidate.getParameterCount() == args.length)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Category deve expor metodo " + methodName));
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Falha ao executar Category." + methodName, exception);
        }
    }
}
