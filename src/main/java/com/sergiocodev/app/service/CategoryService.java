package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.category.CategoryRequest;
import com.sergiocodev.app.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createNewCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategory();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategoryById(Long id, CategoryRequest request);

    void deleteCategoryById(Long id);
}
