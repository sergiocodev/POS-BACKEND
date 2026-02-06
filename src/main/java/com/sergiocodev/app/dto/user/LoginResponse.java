package com.sergiocodev.app.dto.user;

import java.util.Set;

public record LoginResponse(
        String token,
        String refreshToken,
        String type,
        Long id,
        String username,
        String email,
        String fullName,
        String profilePicture,
        Set<String> roles,
        Set<String> permissions) {
    // Constructor without type parameter (defaults to "Bearer")
    public LoginResponse(String token, String refreshToken, Long id, String username, String email,
            String fullName, String profilePicture, Set<String> roles, Set<String> permissions) {
        this(token, refreshToken, "Bearer", id, username, email, fullName, profilePicture, roles, permissions);
    }
}
