package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.permission.CreatePermissionRequest;
import com.sergiocodev.app.dto.permission.PermissionResponse;
import com.sergiocodev.app.service.PermissionService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Permisos", description = "Endpoints para la gestión de permisos del sistema")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Listar todos los permisos", description = "Obtiene la lista de todos los permisos disponibles en el sistema")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<ResponseApi<List<PermissionResponse>>> getAll(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String search) {

        if (module != null && !module.isEmpty()) {
            return ResponseEntity.ok(ResponseApi.success(permissionService.getByModule(module)));
        }

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(ResponseApi.success(permissionService.search(search)));
        }

        return ResponseEntity.ok(ResponseApi.success(permissionService.getAll()));
    }

    @Operation(summary = "Permisos agrupados por módulo", description = "Obtiene los permisos organizados por módulo")
    @GetMapping("/grouped")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<ResponseApi<Map<String, List<PermissionResponse>>>> getGrouped() {
        return ResponseEntity.ok(ResponseApi.success(permissionService.getGroupedByModule()));
    }

    @Operation(summary = "Listar módulos", description = "Obtiene la lista de módulos disponibles")
    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<ResponseApi<List<String>>> getModules() {
        return ResponseEntity.ok(ResponseApi.success(permissionService.getModules()));
    }

    @Operation(summary = "Obtener permiso por ID", description = "Obtiene los detalles de un permiso específico")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<ResponseApi<PermissionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(permissionService.getById(id)));
    }

    @Operation(summary = "Crear nuevo permiso", description = "Crea un nuevo permiso en el sistema")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<ResponseApi<PermissionResponse>> create(@Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse created = permissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(created, "Permiso creado exitosamente"));
    }

    @Operation(summary = "Actualizar permiso", description = "Actualiza la información de un permiso existente")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<ResponseApi<PermissionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(permissionService.update(id, request), "Permiso actualizado exitosamente"));
    }

    @Operation(summary = "Eliminar permiso", description = "Elimina un permiso del sistema")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION + "')")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Permiso eliminado exitosamente"));
    }
}
