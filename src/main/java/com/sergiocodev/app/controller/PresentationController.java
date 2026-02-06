package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.presentation.PresentationRequest;
import com.sergiocodev.app.dto.presentation.PresentationResponse;
import com.sergiocodev.app.service.PresentationService;
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
@RequestMapping("/api/v1/presentations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Presentations", description = "Endpoints para la gestión de presentaciones")
@SecurityRequirement(name = "bearerAuth")
public class PresentationController {

    private final PresentationService service;

    @PostMapping
    @Operation(summary = "Crear presentación")
    public ResponseEntity<ResponseApi<PresentationResponse>> create(@Valid @RequestBody PresentationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Presentación creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar presentaciones")
    public ResponseEntity<ResponseApi<List<PresentationResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener presentación por ID")
    public ResponseEntity<ResponseApi<PresentationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presentación")
    public ResponseEntity<ResponseApi<PresentationResponse>> update(@PathVariable Long id,
            @Valid @RequestBody PresentationRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Presentación actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presentación")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Presentación eliminada exitosamente"));
    }
}
