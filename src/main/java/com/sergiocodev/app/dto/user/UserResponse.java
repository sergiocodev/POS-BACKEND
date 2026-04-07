package com.sergiocodev.app.dto.user;

import com.sergiocodev.app.dto.role.RoleResponse;
import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String profilePicture,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Set<RoleResponse> roles) {
}
