package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.activeingredient.ActiveIngredientRequest;
import com.sergiocodev.app.dto.activeingredient.ActiveIngredientResponse;
import com.sergiocodev.app.service.ActiveIngredientService;
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
@RequestMapping("/api/v1/active-ingredients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Active Ingredients", description = "Endpoints para la gestión de ingredientes activos")
@SecurityRequirement(name = "bearerAuth")
public class ActiveIngredientController {

    private final ActiveIngredientService service;

    @PostMapping
    @Operation(summary = "Crear ingrediente activo")
    public ResponseEntity<ActiveIngredientResponse> create(@Valid @RequestBody ActiveIngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar ingredientes activos")
    public ResponseEntity<List<ActiveIngredientResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar ingredientes activos")
    public ResponseEntity<List<ActiveIngredientResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(service.search(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ingrediente activo por ID")
    public ResponseEntity<ActiveIngredientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ingrediente activo")
    public ResponseEntity<ActiveIngredientResponse> update(@PathVariable Long id,
            @Valid @RequestBody ActiveIngredientRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ingrediente activo")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
