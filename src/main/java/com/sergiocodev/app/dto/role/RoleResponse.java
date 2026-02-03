package com.sergiocodev.app.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private Integer permissionCount;
    private String createdAt;
}
