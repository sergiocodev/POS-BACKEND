package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.user.UserRequest;
import com.sergiocodev.app.dto.user.UserResponse;
import com.sergiocodev.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { RoleMapper.class })
public interface UserMapper {

    UserResponse toResponse(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(UserRequest request);

}
