package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import com.sergiocodev.app.model.Laboratory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LaboratoryMapper {

    LaboratoryResponse toResponse(Laboratory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Laboratory toEntity(LaboratoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(LaboratoryRequest request, @MappingTarget Laboratory entity);
}
