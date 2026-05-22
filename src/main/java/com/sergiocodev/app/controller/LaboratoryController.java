package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import com.sergiocodev.app.service.interfaces.LaboratoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/laboratory")
@RequiredArgsConstructor
@Tag(name = "Laboratories", description = "Endpoints para la gestión del laboratorio")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA_LABORATORIOS + "')")
public class LaboratoryController {

    private final LaboratoryService service;

    @PostMapping("/CreateNewLaboratory")
    @Operation(summary = "Crear laboratorio")
    public ResponseEntity<ResponseApi<LaboratoryResponse>> createNewLaboratory(
            @Valid @RequestBody LaboratoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.createNewLaboratory(request), "Laboratorio creado exitosamente"));
    }

    @GetMapping("/GetAllLaboratory")
    @Operation(summary = "Listar laboratorios")
    public ResponseEntity<ResponseApi<List<LaboratoryResponse>>> getAllLaboratory() {
        return ResponseEntity.ok(ResponseApi.success(service.getAllLaboratory()));
    }

    @GetMapping("/GetLaboratoryById/{id}")
    @Operation(summary = "Obtener laboratorio por ID")
    public ResponseEntity<ResponseApi<LaboratoryResponse>> getLaboratoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getLaboratoryById(id)));
    }

    @PutMapping("/UpdateLaboratoryById/{id}")
    @Operation(summary = "Actualizar laboratorio")
    public ResponseEntity<ResponseApi<LaboratoryResponse>> updateLaboratoryById(@PathVariable Long id,
            @Valid @RequestBody LaboratoryRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.updateLaboratoryById(id, request),
                        "Laboratorio actualizado exitosamente"));
    }

    @DeleteMapping("/DeleteLaboratoryById/{id}")
    @Operation(summary = "Eliminar laboratorio")
    public ResponseEntity<ResponseApi<Void>> deleteLaboratoryById(@PathVariable Long id) {
        service.deleteLaboratoryById(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Laboratorio eliminado exitosamente"));
    }
}
