package com.sergiocodev.app.dto.role;

import com.sergiocodev.app.dto.permission.PermissionResponse;
import java.util.Set;

public record RoleDetailResponse(
        Long id,
        String name,
        String description,
        boolean active,
        Set<PermissionResponse> permissions,
        String createdAt) {
}
