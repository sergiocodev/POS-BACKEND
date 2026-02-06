package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.permission.CreatePermissionRequest;
import com.sergiocodev.app.dto.permission.PermissionResponse;
import com.sergiocodev.app.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Permission toEntity(CreatePermissionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(CreatePermissionRequest request, @MappingTarget Permission entity);
}
