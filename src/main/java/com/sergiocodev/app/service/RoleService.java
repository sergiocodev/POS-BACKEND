package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.permission.PermissionResponse;
import com.sergiocodev.app.dto.role.*;
import com.sergiocodev.app.model.Permission;
import com.sergiocodev.app.model.Role;
import com.sergiocodev.app.repository.PermissionRepository;
import com.sergiocodev.app.repository.RoleRepository;
import com.sergiocodev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Obtener todos los roles
     */
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un rol por ID con sus permisos
     */
    public RoleDetailResponse getById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        return toRoleDetailResponse(role);
    }

    /**
     * Crear un nuevo rol
     */
    @Transactional
    public RoleDetailResponse create(CreateRoleRequest request) {
        // Verificar que no exista un rol con el mismo nombre
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + request.getName());
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setActive(request.isActive());
        role.setPermissions(new HashSet<>());

        Role saved = roleRepository.save(role);
        return toRoleDetailResponse(saved);
    }

    /**
     * Actualizar un rol
     */
    @Transactional
    public RoleDetailResponse update(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Verificar que no exista otro rol con el mismo nombre
        roleRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Ya existe otro rol con el nombre: " + request.getName());
            }
        });

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setActive(request.isActive());

        Role updated = roleRepository.save(role);
        return toRoleDetailResponse(updated);
    }

    /**
     * Eliminar un rol
     */
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Verificar que el rol no esté asignado a ningún usuario
        if (userRepository.existsByRoles_Id(id)) {
            throw new RuntimeException("No se puede eliminar el rol '" + role.getName() +
                    "' porque está asignado a uno o más usuarios");
        }

        roleRepository.delete(role);
    }

    /**
     * Activar/desactivar un rol
     */
    @Transactional
    public RoleResponse toggleActive(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        role.setActive(!role.isActive());
        Role updated = roleRepository.save(role);
        return toRoleResponse(updated);
    }

    /**
     * Obtener permisos de un rol
     */
    public List<PermissionResponse> getPermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));

        return role.getPermissions().stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Asignar permisos a un rol (agregar a los existentes)
     */
    @Transactional
    public RoleDetailResponse assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));

        Set<Permission> permissionsToAdd = new HashSet<>();
        for (Long permissionId : request.getPermissionIds()) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permissionId));
            permissionsToAdd.add(permission);
        }

        role.getPermissions().addAll(permissionsToAdd);
        Role updated = roleRepository.save(role);
        return toRoleDetailResponse(updated);
    }

    /**
     * Reemplazar todos los permisos de un rol
     */
    @Transactional
    public RoleDetailResponse replacePermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));

        Set<Permission> newPermissions = new HashSet<>();
        for (Long permissionId : request.getPermissionIds()) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permissionId));
            newPermissions.add(permission);
        }

        role.setPermissions(newPermissions);
        Role updated = roleRepository.save(role);
        return toRoleDetailResponse(updated);
    }

    /**
     * Remover un permiso específico de un rol
     */
    @Transactional
    public RoleDetailResponse removePermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permissionId));

        role.getPermissions().remove(permission);
        Role updated = roleRepository.save(role);
        return toRoleDetailResponse(updated);
    }

    /**
     * Remover múltiples permisos de un rol
     */
    @Transactional
    public RoleDetailResponse removePermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));

        for (Long permissionId : request.getPermissionIds()) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permissionId));
            role.getPermissions().remove(permission);
        }

        Role updated = roleRepository.save(role);
        return toRoleDetailResponse(updated);
    }

    /**
     * Convertir Role a RoleResponse
     */
    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getPermissions().size(),
                role.getCreatedAt().format(FORMATTER));
    }

    /**
     * Convertir Role a RoleDetailResponse
     */
    private RoleDetailResponse toRoleDetailResponse(Role role) {
        Set<PermissionResponse> permissions = role.getPermissions().stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toSet());

        return new RoleDetailResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                permissions,
                role.getCreatedAt().format(FORMATTER));
    }

    /**
     * Convertir Permission a PermissionResponse
     */
    private PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getModule(),
                permission.getCreatedAt().format(FORMATTER));
    }
}
