package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.establishment.EstablishmentRequest;
import com.sergiocodev.app.dto.establishment.EstablishmentResponse;
import com.sergiocodev.app.model.Establishment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EstablishmentMapper {

    EstablishmentResponse toResponse(Establishment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Establishment toEntity(EstablishmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(EstablishmentRequest request, @MappingTarget Establishment entity);
}
