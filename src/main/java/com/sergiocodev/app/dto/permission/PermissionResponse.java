package com.sergiocodev.app.dto.permission;

public record PermissionResponse(
        Long id,
        String name,
        String description,
        String module,
        String createdAt) {
}
