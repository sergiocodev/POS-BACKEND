package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormRequest;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormResponse;
import com.sergiocodev.app.model.PharmaceuticalForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PharmaceuticalFormMapper {

    PharmaceuticalFormResponse toResponse(PharmaceuticalForm pharmaceuticalForm);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    PharmaceuticalForm toEntity(PharmaceuticalFormRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(PharmaceuticalFormRequest request, @MappingTarget PharmaceuticalForm pharmaceuticalForm);
}
