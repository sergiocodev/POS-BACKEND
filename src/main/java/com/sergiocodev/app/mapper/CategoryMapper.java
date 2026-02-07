package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.category.CategoryRequest;
import com.sergiocodev.app.dto.category.CategoryResponse;
import com.sergiocodev.app.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(CategoryRequest request, @MappingTarget Category category);
}
