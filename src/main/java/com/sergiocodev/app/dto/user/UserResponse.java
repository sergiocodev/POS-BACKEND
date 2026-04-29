package com.sergiocodev.app.dto.user;

import com.sergiocodev.app.dto.role.RoleResponse;
import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String profilePicture,
        Instant lastLogin,
        Instant createdAt,
        Instant updatedAt,
        Set<RoleResponse> roles) {
}
