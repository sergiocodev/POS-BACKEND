package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionRequest;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionResponse;
import com.sergiocodev.app.model.TherapeuticAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TherapeuticActionMapper {

    TherapeuticActionResponse toResponse(TherapeuticAction entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TherapeuticAction toEntity(TherapeuticActionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(TherapeuticActionRequest request, @MappingTarget TherapeuticAction entity);
}
