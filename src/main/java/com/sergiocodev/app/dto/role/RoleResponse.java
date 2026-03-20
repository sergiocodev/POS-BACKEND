package com.sergiocodev.app.dto.role;

public record RoleResponse(
                Long id,
                String name,
                String description,
                Integer permissionCount,
                String createdAt) {
}
