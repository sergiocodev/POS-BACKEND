package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormRequest;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormResponse;
import com.sergiocodev.app.service.interfaces.PharmaceuticalFormService;
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
@RequestMapping("/api/v1/pharmaceutical-forms")
@RequiredArgsConstructor
@Tag(name = "Pharmaceutical Forms", description = "Endpoints para la gestión de formas farmacéuticas")
@SecurityRequirement(name = "bearerAuth")
public class PharmaceuticalFormController {

    private final PharmaceuticalFormService service;

    @PostMapping
    @Operation(summary = "Crear forma farmacéutica")
    @RequiresPermission(PermissionConstants.FARMACIA_FORMAS)
    public ResponseEntity<ResponseApi<PharmaceuticalFormResponse>> create(
            @Valid @RequestBody PharmaceuticalFormRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Forma farmacéutica creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar formas farmacéuticas")
    public ResponseEntity<ResponseApi<List<PharmaceuticalFormResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.findAll()));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar formas farmacéuticas paginadas")
    public ResponseEntity<ResponseApi<Page<PharmaceuticalFormResponse>>> getPaged(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.findAllPaged(name, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener forma farmacéutica por ID")
    public ResponseEntity<ResponseApi<PharmaceuticalFormResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar forma farmacéutica")
    @RequiresPermission(PermissionConstants.FARMACIA_FORMAS)
    public ResponseEntity<ResponseApi<PharmaceuticalFormResponse>> update(@PathVariable Long id,
            @Valid @RequestBody PharmaceuticalFormRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Forma farmacéutica actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar forma farmacéutica")
    @RequiresPermission(PermissionConstants.FARMACIA_FORMAS)
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Forma farmacéutica eliminada exitosamente"));
    }
}
