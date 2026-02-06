package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.presentation.PresentationRequest;
import com.sergiocodev.app.dto.presentation.PresentationResponse;
import com.sergiocodev.app.model.Presentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PresentationMapper {

    PresentationResponse toResponse(Presentation entity);

    @Mapping(target = "id", ignore = true)
    Presentation toEntity(PresentationRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(PresentationRequest request, @MappingTarget Presentation entity);
}
