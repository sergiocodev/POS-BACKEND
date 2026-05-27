package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.permission.PermissionResponse;
import com.sergiocodev.app.dto.role.*;
import java.util.List;

public interface RoleService {

    List<RoleResponse> getAll();

    RoleDetailResponse getById(Long id);

    RoleDetailResponse create(CreateRoleRequest request);

    RoleDetailResponse update(Long id, UpdateRoleRequest request);

    void delete(Long id);

    RoleResponse toggleActive(Long id);

    List<PermissionResponse> getPermissions(Long roleId);

    RoleDetailResponse assignPermissions(Long roleId, AssignPermissionsRequest request);

    RoleDetailResponse replacePermissions(Long roleId, AssignPermissionsRequest request);

    RoleDetailResponse removePermission(Long roleId, Long permissionId);

    RoleDetailResponse removePermissions(Long roleId, AssignPermissionsRequest request);
}
