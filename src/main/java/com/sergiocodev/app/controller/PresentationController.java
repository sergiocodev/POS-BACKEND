package com.sergiocodev.app.controller;

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
@RequestMapping("/api/presentations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Presentations", description = "Endpoints para la gestión de presentaciones")
@SecurityRequirement(name = "bearerAuth")
public class PresentationController {

    private final PresentationService service;

    @PostMapping
    @Operation(summary = "Crear presentación")
    public ResponseEntity<PresentationResponse> create(@Valid @RequestBody PresentationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar presentaciones")
    public ResponseEntity<List<PresentationResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener presentación por ID")
    public ResponseEntity<PresentationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presentación")
    public ResponseEntity<PresentationResponse> update(@PathVariable Long id,
            @Valid @RequestBody PresentationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presentación")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
