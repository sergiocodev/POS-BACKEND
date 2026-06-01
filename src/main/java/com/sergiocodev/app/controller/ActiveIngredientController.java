package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.activeingredient.ActiveIngredientRequest;
import com.sergiocodev.app.dto.activeingredient.ActiveIngredientResponse;
import com.sergiocodev.app.service.interfaces.ActiveIngredientService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/active-ingredients")
@RequiredArgsConstructor
@Tag(name = "Active Ingredients", description = "Endpoints para la gestión de ingredientes activos")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA_PRINCIPIOS_ACTIVOS + "')")
public class ActiveIngredientController {

    private final ActiveIngredientService service;

    @PostMapping("/CreateNewActiveIngredient")
    @Operation(summary = "Crear ingrediente activo")
    public ResponseEntity<ResponseApi<ActiveIngredientResponse>> create(
            @Valid @RequestBody ActiveIngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Ingrediente activo creado exitosamente"));
    }

    @GetMapping("/GetAllActiveIngredients")
    @Operation(summary = "Listar ingredientes activos")
    public ResponseEntity<ResponseApi<List<ActiveIngredientResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping
    @Operation(summary = "Listar ingredientes activos paginados")
    public ResponseEntity<ResponseApi<Page<ActiveIngredientResponse>>> getPaged(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.findAllPaged(name, description, pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar ingredientes activos")
    public ResponseEntity<ResponseApi<List<ActiveIngredientResponse>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ResponseApi.success(service.search(query)));
    }

    @GetMapping("/GetActiveIngredientById/{id}")
    @Operation(summary = "Obtener ingrediente activo por ID")
    public ResponseEntity<ResponseApi<ActiveIngredientResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/UpdateActiveIngredientById/{id}")
    @Operation(summary = "Actualizar ingrediente activo")
    public ResponseEntity<ResponseApi<ActiveIngredientResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ActiveIngredientRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Ingrediente activo actualizado exitosamente"));
    }

    @DeleteMapping("/DeleteActiveIngredientById/{id}")
    @Operation(summary = "Eliminar ingrediente activo")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Ingrediente activo eliminado exitosamente"));
    }
}
