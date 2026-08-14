package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.establishment.EstablishmentRequest;
import com.sergiocodev.app.dto.establishment.EstablishmentResponse;
import com.sergiocodev.app.service.interfaces.EstablishmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/establishments")
@RequiredArgsConstructor
@Tag(name = "Establishments", description = "Endpoints para la gestión de establecimientos")
@SecurityRequirement(name = "bearerAuth")
public class EstablishmentController {

    private final EstablishmentService service;

    @PostMapping
    @Operation(summary = "Crear establecimiento")
    @RequiresPermission(PermissionConstants.CONFIGURACION_ESTABLECIMIENTOS)
    public ResponseEntity<ResponseApi<EstablishmentResponse>> create(@Valid @RequestBody EstablishmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Establecimiento creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar establecimientos")
    public ResponseEntity<ResponseApi<List<EstablishmentResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/paged")
    @Operation(summary = "Lista de establecimientos con paginación")
    @RequiresPermission(PermissionConstants.CONFIGURACION_ESTABLECIMIENTOS)
    public ResponseEntity<ResponseApi<org.springframework.data.domain.Page<EstablishmentResponse>>> getAllPaged(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String codeSunat,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.getAllPaged(name, codeSunat, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener establecimiento por ID")
    public ResponseEntity<ResponseApi<EstablishmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar establecimiento")
    @RequiresPermission(PermissionConstants.CONFIGURACION_ESTABLECIMIENTOS)
    public ResponseEntity<ResponseApi<EstablishmentResponse>> update(@PathVariable Long id,
            @Valid @RequestBody EstablishmentRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Establecimiento actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar establecimiento")
    @RequiresPermission(PermissionConstants.CONFIGURACION_ESTABLECIMIENTOS)
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Establecimiento eliminado exitosamente"));
    }
}
