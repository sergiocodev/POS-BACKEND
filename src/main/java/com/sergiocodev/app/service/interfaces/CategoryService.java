package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.category.CategoryRequest;
import com.sergiocodev.app.dto.category.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryResponse createNewCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategory();

    Page<CategoryResponse> findAllPaged(String name, Pageable pageable);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategoryById(Long id, CategoryRequest request);

    void deleteCategoryById(Long id);
}
