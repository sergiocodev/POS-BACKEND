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
        if (request.prefix() != null && !request.prefix().isBlank()) {
            String cleanPrefix = request.prefix().trim().toUpperCase();
            if (categoryRepository.existsByPrefix(cleanPrefix)) {
                throw new DuplicateCategoryException("Category prefix already exists: " + cleanPrefix);
            }
            category.setPrefix(cleanPrefix);
        } else {
            category.setPrefix(generateUniquePrefix(request.name()));
        }

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

    private String generateUniquePrefix(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return "CAT";
        }
        String normalized = java.text.Normalizer.normalize(categoryName.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toUpperCase();

        java.util.Set<String> stopWords = java.util.Set.of("DE", "DEL", "LA", "EL", "LOS", "LAS", "Y", "EN", "PARA", "POR", "O", "A", "CON");
        String[] words = normalized.split("[^A-Z0-9]+");
        StringBuilder initials = new StringBuilder();

        for (String word : words) {
            if (!word.isBlank() && !stopWords.contains(word)) {
                initials.append(word.charAt(0));
            }
        }

        String basePrefix;
        if (initials.length() >= 2) {
            basePrefix = initials.substring(0, Math.min(initials.length(), 4));
        } else if (words.length > 0 && !words[0].isBlank()) {
            String firstWord = words[0];
            basePrefix = firstWord.substring(0, Math.min(firstWord.length(), 3));
        } else {
            basePrefix = "CAT";
        }

        if (!categoryRepository.existsByPrefix(basePrefix)) {
            return basePrefix;
        }

        int counter = 2;
        String candidate = basePrefix + counter;
        while (categoryRepository.existsByPrefix(candidate)) {
            counter++;
            candidate = basePrefix + counter;
        }
        return candidate;
    }
}
