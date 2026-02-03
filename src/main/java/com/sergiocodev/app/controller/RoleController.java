package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.permission.PermissionResponse;
import com.sergiocodev.app.dto.role.*;
import com.sergiocodev.app.service.RoleService;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Roles", description = "Endpoints para la gestión de roles y permisos")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Listar todos los roles", description = "Obtiene la lista de todos los roles disponibles")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<List<RoleResponse>> getAll() {
        return ResponseEntity.ok(roleService.getAll());
    }

    @Operation(summary = "Obtener rol por ID", description = "Obtiene los detalles de un rol específico con sus permisos")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @Operation(summary = "Crear nuevo rol", description = "Crea un nuevo rol en el sistema")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleDetailResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        RoleDetailResponse created = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar rol", description = "Actualiza la información de un rol existente")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    @Operation(summary = "Eliminar rol", description = "Elimina un rol del sistema")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar/desactivar rol", description = "Cambia el estado activo de un rol")
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.toggleActive(id));
    }

    // ==================== Gestión de Permisos ====================

    @Operation(summary = "Obtener permisos de un rol", description = "Obtiene la lista de permisos asignados a un rol")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<List<PermissionResponse>> getPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getPermissions(id));
    }

    @Operation(summary = "Asignar permisos a un rol", description = "Agrega permisos a un rol (se suman a los existentes)")
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleDetailResponse> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        return ResponseEntity.ok(roleService.assignPermissions(id, request));
    }

    @Operation(summary = "Reemplazar permisos de un rol", description = "Reemplaza todos los permisos de un rol con los nuevos")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleDetailResponse> replacePermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        return ResponseEntity.ok(roleService.replacePermissions(id, request));
    }

    @Operation(summary = "Remover un permiso de un rol", description = "Elimina un permiso específico de un rol")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleDetailResponse> removePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.removePermission(roleId, permissionId));
    }

    @Operation(summary = "Remover múltiples permisos", description = "Elimina varios permisos de un rol")
    @PostMapping("/{id}/permissions/batch-remove")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<RoleDetailResponse> removePermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        return ResponseEntity.ok(roleService.removePermissions(id, request));
    }
}
