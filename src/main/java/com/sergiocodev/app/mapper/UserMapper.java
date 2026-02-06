package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.user.UserResponse;
import com.sergiocodev.app.model.Role;
import com.sergiocodev.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    UserResponse toResponse(User entity);

    @Named("mapRoles")
    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null)
            return null;
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
