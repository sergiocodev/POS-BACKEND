package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.establishment.EstablishmentRequest;
import com.sergiocodev.app.dto.establishment.EstablishmentResponse;
import com.sergiocodev.app.service.EstablishmentService;
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
@RequestMapping("/api/v1/establishments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Establishments", description = "Endpoints para la gestión de establecimientos")
@SecurityRequirement(name = "bearerAuth")
public class EstablishmentController {

    private final EstablishmentService service;

    @PostMapping
    @Operation(summary = "Crear establecimiento")
    public ResponseEntity<EstablishmentResponse> create(@Valid @RequestBody EstablishmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar establecimientos")
    public ResponseEntity<List<EstablishmentResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener establecimiento por ID")
    public ResponseEntity<EstablishmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar establecimiento")
    public ResponseEntity<EstablishmentResponse> update(@PathVariable Long id,
            @Valid @RequestBody EstablishmentRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar establecimiento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
