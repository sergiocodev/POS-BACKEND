package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import com.sergiocodev.app.service.LaboratoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/laboratories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Laboratories", description = "Endpoints para la gestión del laboratorio")
@SecurityRequirement(name = "bearerAuth")
public class LaboratoryController {

    private final LaboratoryService service;

    @PostMapping
    @Operation(summary = "Crear laboratorio")
    public ResponseEntity<ResponseApi<LaboratoryResponse>> create(@Valid @RequestBody LaboratoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Laboratorio creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar laboratorios")
    public ResponseEntity<ResponseApi<List<LaboratoryResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener laboratorio por ID")
    public ResponseEntity<ResponseApi<LaboratoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar laboratorio")
    public ResponseEntity<ResponseApi<LaboratoryResponse>> update(@PathVariable Long id,
            @Valid @RequestBody LaboratoryRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Laboratorio actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar laboratorio")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Laboratorio eliminado exitosamente"));
    }
}
