package com.zorysa.finance.categories.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.categories.dto.CategoryCountResponse;
import com.zorysa.finance.categories.dto.CategoryResponse;
import com.zorysa.finance.categories.dto.CategoryTypeCountsResponse;
import com.zorysa.finance.categories.dto.CreateCategoryRequest;
import com.zorysa.finance.categories.dto.UpdateCategoryRequest;
import com.zorysa.finance.categories.entity.CategoryType;
import com.zorysa.finance.categories.service.CategoryService;
import com.zorysa.finance.shared.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUser currentUser;

    public CategoryController(CategoryService categoryService, CurrentUser currentUser) {
        this.categoryService = categoryService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<CategoryResponse> listCategories(@RequestParam(required = false) CategoryType type, Pageable pageable) {
        return PageResponse.from(categoryService.listCategories(currentUser.id(), type, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(currentUser.id(), request);
    }

    @GetMapping("/custom/count")
    CategoryCountResponse countCustomCategories() {
        return categoryService.countCustomCategories(currentUser.id());
    }

    @GetMapping("/type-counts")
    CategoryTypeCountsResponse countCategoriesByType() {
        return categoryService.countCategoriesByType(currentUser.id());
    }

    @GetMapping("/{id}")
    CategoryResponse getCategory(@PathVariable UUID id) {
        return categoryService.getCategory(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    CategoryResponse updateCategory(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.updateCategory(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(currentUser.id(), id);
    }
}
