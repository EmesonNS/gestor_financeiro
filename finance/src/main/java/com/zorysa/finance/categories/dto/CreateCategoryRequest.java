package com.zorysa.finance.categories.dto;

import com.zorysa.finance.categories.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull CategoryType type,
        @Size(max = 20) @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "must be a hex color") String color,
        @Size(max = 80) String icon
) {
}
