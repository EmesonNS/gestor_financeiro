package com.zorysa.finance.categories.dto;

import com.zorysa.finance.categories.entity.CategoryType;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        String color,
        String icon,
        boolean defaultCategory,
        Instant createdAt,
        Instant updatedAt
) {
}
