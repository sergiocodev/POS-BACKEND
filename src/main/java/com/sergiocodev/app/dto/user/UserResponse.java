package com.sergiocodev.app.dto.user;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String profilePicture,
        boolean active,
        Set<String> roles) {
}
