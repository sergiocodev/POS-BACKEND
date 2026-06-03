package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionRequest;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionResponse;
import com.sergiocodev.app.service.interfaces.TherapeuticActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/therapeutic-actions")
@RequiredArgsConstructor
@Tag(name = "Therapeutic Actions", description = "Endpoints para la gestión de acciones terapéuticas")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.FARMACIA_ACCIONES)
public class TherapeuticActionController {

    private final TherapeuticActionService service;

    @PostMapping
    @Operation(summary = "Crear acción terapéutica")
    public ResponseEntity<ResponseApi<TherapeuticActionResponse>> create(
            @Valid @RequestBody TherapeuticActionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Acción terapéutica creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar acciones terapéuticas")
    public ResponseEntity<ResponseApi<List<TherapeuticActionResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.findAll()));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar acciones terapéuticas paginadas")
    public ResponseEntity<ResponseApi<Page<TherapeuticActionResponse>>> getPaged(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.findAllPaged(name, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener acción terapéutica por ID")
    public ResponseEntity<ResponseApi<TherapeuticActionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar acción terapéutica")
    public ResponseEntity<ResponseApi<TherapeuticActionResponse>> update(@PathVariable Long id,
            @Valid @RequestBody TherapeuticActionRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Acción terapéutica actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar acción terapéutica")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Acción terapéutica eliminada exitosamente"));
    }
}
