package com.zorysa.finance.categories.mapper;

import com.zorysa.finance.categories.dto.CategoryResponse;
import com.zorysa.finance.categories.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColor(),
                category.getIcon(),
                category.isDefault(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
