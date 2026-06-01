package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.CategoryService;

import com.sergiocodev.app.dto.category.CategoryRequest;
import com.sergiocodev.app.dto.category.CategoryResponse;
import com.sergiocodev.app.exception.CategoryNotFoundException;
import com.sergiocodev.app.exception.DuplicateCategoryException;
import com.sergiocodev.app.mapper.CategoryMapper;
import com.sergiocodev.app.model.Category;
import com.sergiocodev.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createNewCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateCategoryException("Category already exists with name: " + request.name());
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategory() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAllPaged(String name, Pageable pageable) {
        return categoryRepository.findAllPaged(name, pageable)
                .map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategoryById(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

        if (!category.getName().equals(request.name()) &&
                categoryRepository.existsByName(request.name())) {
            throw new DuplicateCategoryException("Category already exists with name: " + request.name());
        }

        categoryMapper.updateEntity(request, category);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
