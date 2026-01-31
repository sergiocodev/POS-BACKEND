package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.inventory.InventoryRequest;
import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Inventory", description = "Endpoints para la gestión de inventario")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/update")
    @Operation(summary = "Actualizar nivel de stock manualmente")
    public ResponseEntity<InventoryResponse> updateStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(service.updateStock(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos los registros de inventario")
    public ResponseEntity<List<InventoryResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/establishment/{establishmentId}")
    @Operation(summary = "Listar inventario por establecimiento")
    public ResponseEntity<List<InventoryResponse>> getByEstablishment(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(service.getByEstablishment(establishmentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de inventario por ID")
    public ResponseEntity<InventoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
