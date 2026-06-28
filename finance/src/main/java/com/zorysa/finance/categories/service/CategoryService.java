package com.zorysa.finance.categories.service;

import com.zorysa.finance.categories.dto.CategoryCountResponse;
import com.zorysa.finance.categories.dto.CategoryResponse;
import com.zorysa.finance.categories.dto.CategoryTypeCountsResponse;
import com.zorysa.finance.categories.dto.CreateCategoryRequest;
import com.zorysa.finance.categories.dto.UpdateCategoryRequest;
import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.entity.CategoryType;
import com.zorysa.finance.categories.mapper.CategoryMapper;
import com.zorysa.finance.categories.repository.CategoryRepository;
import com.zorysa.finance.shared.exception.BadRequestException;
import com.zorysa.finance.shared.exception.ConflictException;
import com.zorysa.finance.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final ObjectProvider<CategoryRepository> categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(ObjectProvider<CategoryRepository> categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> listCategories(UUID userId, CategoryType type, Pageable pageable) {
        Page<Category> categories = type == null
                ? repository().findAllByUserIdOrDefault(userId, pageable)
                : repository().findAllByUserIdOrDefaultAndType(userId, type, pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryCountResponse countCustomCategories(UUID userId) {
        return new CategoryCountResponse(repository().countByUserIdAndDefaultCategoryFalse(userId));
    }

    @Transactional(readOnly = true)
    public CategoryTypeCountsResponse countCategoriesByType(UUID userId) {
        return new CategoryTypeCountsResponse(
                repository().countApplicableByUserIdAndType(userId, CategoryType.INCOME),
                repository().countApplicableByUserIdAndType(userId, CategoryType.EXPENSE)
        );
    }

    @Transactional
    public CategoryResponse createCategory(UUID userId, CreateCategoryRequest request) {
        String name = request.name().trim();
        if (repository().existsByUserIdAndNameAndType(userId, name, request.type())) {
            throw new ConflictException("Categoria ja cadastrada");
        }
        Category category = new Category(
                userId,
                name,
                request.type(),
                normalizeOptional(request.color()),
                normalizeOptional(request.icon()),
                false
        );
        return categoryMapper.toResponse(repository().save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID userId, UUID categoryId) {
        return categoryMapper.toResponse(findUsableCategory(userId, categoryId));
    }

    @Transactional
    public CategoryResponse updateCategory(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        Category category = findOwnedCategory(userId, categoryId);
        String name = request.name().trim();
        if ((!category.getName().equals(name) || category.getType() != request.type())
                && repository().existsByUserIdAndNameAndType(userId, name, request.type())) {
            throw new ConflictException("Categoria ja cadastrada");
        }
        category.updateDetails(name, request.type(), normalizeOptional(request.color()), normalizeOptional(request.icon()));
        return categoryMapper.toResponse(repository().save(category));
    }

    @Transactional
    public void deleteCategory(UUID userId, UUID categoryId) {
        Category category = findOwnedCategory(userId, categoryId);
        repository().delete(category);
    }

    private Category findUsableCategory(UUID userId, UUID categoryId) {
        return repository().findByIdAndUserIdOrDefault(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
    }

    private Category findOwnedCategory(UUID userId, UUID categoryId) {
        Category category = findUsableCategory(userId, categoryId);
        if (!category.belongsTo(userId)) {
            throw new BadRequestException("Categoria padrao nao pode ser alterada");
        }
        return category;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CategoryRepository repository() {
        return categoryRepository.getIfAvailable(() -> {
            throw new IllegalStateException("CategoryRepository nao disponivel");
        });
    }
}
