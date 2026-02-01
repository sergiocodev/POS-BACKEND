package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.taxtype.TaxTypeRequest;
import com.sergiocodev.app.dto.taxtype.TaxTypeResponse;
import com.sergiocodev.app.service.TaxTypeService;
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
@RequestMapping("/api/v1/tax-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Tax Types", description = "Endpoints para la gestión del tipo de impuesto")
@SecurityRequirement(name = "bearerAuth")
public class TaxTypeController {

    private final TaxTypeService service;

    @PostMapping
    @Operation(summary = "Crear tipo de impuesto")
    public ResponseEntity<TaxTypeResponse> create(@Valid @RequestBody TaxTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos los tipos de impuesto")
    public ResponseEntity<List<TaxTypeResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el tipo de impuesto por ID")
    public ResponseEntity<TaxTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de impuesto")
    public ResponseEntity<TaxTypeResponse> update(@PathVariable Long id, @Valid @RequestBody TaxTypeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de impuesto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
