package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.brand.BrandRequest;
import com.sergiocodev.app.dto.brand.BrandResponse;
import com.sergiocodev.app.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    BrandResponse toResponse(Brand entity);

    @Mapping(target = "id", ignore = true)
    Brand toEntity(BrandRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(BrandRequest request, @MappingTarget Brand entity);
}
