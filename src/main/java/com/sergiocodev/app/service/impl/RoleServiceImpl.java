package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.permission.PermissionResponse;
import com.sergiocodev.app.dto.role.*;
import com.sergiocodev.app.mapper.RoleMapper;
import com.sergiocodev.app.model.Permission;
import com.sergiocodev.app.model.Role;
import com.sergiocodev.app.repository.PermissionRepository;
import com.sergiocodev.app.repository.RoleRepository;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllPaged(String searchTerm, Pageable pageable) {
        return roleRepository.findAllActiveFiltered(searchTerm, pageable)
                .map(roleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
        return roleMapper.toDetailResponse(role);
    }

    @Override
    @Transactional
    public RoleDetailResponse create(CreateRoleRequest request) {
        if (roleRepository.findByName(request.name()).isPresent()) {
            throw new BadRequestException("Ya existe un rol con el nombre: " + request.name());
        }

        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(new HashSet<>());

        Role saved = roleRepository.save(role);
        return roleMapper.toDetailResponse(saved);
    }

    @Override
    @Transactional
    public RoleDetailResponse update(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        roleRepository.findByName(request.name()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Ya existe otro rol con el nombre: " + request.name());
            }
        });

        role.setName(request.name());
        role.setDescription(request.description());

        Role updated = roleRepository.save(role);
        return roleMapper.toDetailResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        if (userRepository.existsByRoles_Id(id)) {
            throw new BadRequestException("No se puede eliminar el rol '" + role.getName() +
                    "' porque está asignado a uno o más usuarios");
        }

        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleResponse toggleActive(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        if (role.getDeletedAt() == null) {
            role.setDeletedAt(java.time.LocalDateTime.now());
        } else {
            role.setDeletedAt(null);
        }
        Role updated = roleRepository.save(role);
        return roleMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));

        return role.getPermissions().stream()
                .map(permission -> new PermissionResponse(
                        permission.getId(),
                        permission.getName(),
                        permission.getDescription(),
                        permission.getModule(),
                        permission.getCreatedAt() != null ? permission.getCreatedAt().toString() : null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDetailResponse assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));

        return handlePermissions(role, request.permissionIds(), false);
    }

    @Override
    @Transactional
    public RoleDetailResponse replacePermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));

        return handlePermissions(role, request.permissionIds(), true);
    }

    private RoleDetailResponse handlePermissions(Role role, Set<Long> permissionIds, boolean replace) {
        Set<Permission> permissions = new HashSet<>();
        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + permissionId));
            permissions.add(permission);
        }

        if (replace) {
            role.setPermissions(permissions);
        } else {
            role.getPermissions().addAll(permissions);
        }

        Role updated = roleRepository.save(role);
        return roleMapper.toDetailResponse(updated);
    }

    @Override
    @Transactional
    public RoleDetailResponse removePermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + permissionId));

        role.getPermissions().remove(permission);
        Role updated = roleRepository.save(role);
        return roleMapper.toDetailResponse(updated);
    }

    @Override
    @Transactional
    public RoleDetailResponse removePermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));

        for (Long permissionId : request.permissionIds()) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + permissionId));
            role.getPermissions().remove(permission);
        }

        Role updated = roleRepository.save(role);
        return roleMapper.toDetailResponse(updated);
    }
}
