package com.sergiocodev.app.dto.role;

import com.sergiocodev.app.dto.permission.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private Set<PermissionResponse> permissions;
    private String createdAt;
}
