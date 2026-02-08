package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.permission.CreatePermissionRequest;
import com.sergiocodev.app.dto.permission.PermissionResponse;
import com.sergiocodev.app.model.Permission;
import com.sergiocodev.app.repository.PermissionRepository;
import com.sergiocodev.app.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Obtener todos los permisos
     */
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener permisos filtrados por módulo
     */
    public List<PermissionResponse> getByModule(String module) {
        return permissionRepository.findByModule(module).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Buscar permisos por nombre o descripción
     */
    public List<PermissionResponse> search(String query) {
        return permissionRepository.findByNameContainingOrDescriptionContaining(query, query).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener permisos agrupados por módulo
     */
    public Map<String, List<PermissionResponse>> getGroupedByModule() {
        return permissionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.groupingBy(
                        p -> p.module() != null ? p.module() : "SIN_MODULO"));
    }

    /**
     * Obtener lista de módulos únicos
     */
    public List<String> getModules() {
        return permissionRepository.findAll().stream()
                .map(Permission::getModule)
                .filter(module -> module != null && !module.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Obtener un permiso por ID
     */
    public PermissionResponse getById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));
        return toResponse(permission);
    }

    /**
     * Crear un nuevo permiso
     */
    @Transactional
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.findByName(request.name()).isPresent()) {
            throw new BadRequestException("Ya existe un permiso con el nombre: " + request.name());
        }

        Permission permission = new Permission();
        permission.setName(request.name());
        permission.setDescription(request.description());
        permission.setModule(request.module());

        Permission saved = permissionRepository.save(permission);
        return toResponse(saved);
    }

    /**
     * Actualizar un permiso
     */
    @Transactional
    public PermissionResponse update(Long id, CreatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));

        permissionRepository.findByName(request.name()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Ya existe otro permiso con el nombre: " + request.name());
            }
        });

        permission.setName(request.name());
        permission.setDescription(request.description());
        permission.setModule(request.module());

        Permission updated = permissionRepository.save(permission);
        return toResponse(updated);
    }

    /**
     * Eliminar un permiso
     */
    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));

        if (roleRepository.existsByPermissions_Id(id)) {
            throw new BadRequestException("No se puede eliminar el permiso '" + permission.getName() +
                    "' porque está asignado a uno o más roles");
        }

        permissionRepository.delete(permission);
    }

    /**
     * Convertir entidad a DTO
     */
    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getModule(),
                permission.getCreatedAt().format(FORMATTER));
    }
}
