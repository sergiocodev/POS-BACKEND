package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.user.UserResponse;
import com.sergiocodev.app.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { RoleMapper.class })
public interface UserMapper {

    UserResponse toResponse(User entity);

}
