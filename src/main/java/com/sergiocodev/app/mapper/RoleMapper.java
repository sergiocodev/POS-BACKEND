package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.role.RoleDetailResponse;
import com.sergiocodev.app.dto.role.RoleResponse;
import com.sergiocodev.app.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissionCount", expression = "java(entity.getPermissions() != null ? entity.getPermissions().size() : 0)")
    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)")
    RoleResponse toResponse(Role entity);

    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)")
    RoleDetailResponse toDetailResponse(Role entity);
}
