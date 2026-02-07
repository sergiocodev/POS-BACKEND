package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.activeingredient.ActiveIngredientRequest;
import com.sergiocodev.app.dto.activeingredient.ActiveIngredientResponse;
import com.sergiocodev.app.model.ActiveIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ActiveIngredientMapper {

    ActiveIngredientResponse toResponse(ActiveIngredient entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ActiveIngredient toEntity(ActiveIngredientRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(ActiveIngredientRequest request, @MappingTarget ActiveIngredient entity);
}
