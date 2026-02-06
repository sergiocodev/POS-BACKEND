package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
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
    public ResponseEntity<ResponseApi<ActiveIngredientResponse>> create(
            @Valid @RequestBody ActiveIngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Ingrediente activo creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar ingredientes activos")
    public ResponseEntity<ResponseApi<List<ActiveIngredientResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar ingredientes activos")
    public ResponseEntity<ResponseApi<List<ActiveIngredientResponse>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ResponseApi.success(service.search(query)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ingrediente activo por ID")
    public ResponseEntity<ResponseApi<ActiveIngredientResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ingrediente activo")
    public ResponseEntity<ResponseApi<ActiveIngredientResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ActiveIngredientRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Ingrediente activo actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ingrediente activo")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Ingrediente activo eliminado exitosamente"));
    }
}
