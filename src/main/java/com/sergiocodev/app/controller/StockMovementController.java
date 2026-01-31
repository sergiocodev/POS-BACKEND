package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.stockmovement.StockMovementRequest;
import com.sergiocodev.app.dto.stockmovement.StockMovementResponse;
import com.sergiocodev.app.service.StockMovementService;
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
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Stock Movements", description = "Endpoints para la gestión de movimientos de stock")
@SecurityRequirement(name = "bearerAuth")
public class StockMovementController {

    private final StockMovementService service;

    @PostMapping
    @Operation(summary = "Crear movimiento manual de stock")
    public ResponseEntity<StockMovementResponse> create(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos los movimientos de stock")
    public ResponseEntity<List<StockMovementResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Listar movimientos por producto")
    public ResponseEntity<List<StockMovementResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(service.getByProduct(productId));
    }

    @GetMapping("/establishment/{establishmentId}")
    @Operation(summary = "Listar movimientos por establecimiento")
    public ResponseEntity<List<StockMovementResponse>> getByEstablishment(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(service.getByEstablishment(establishmentId));
    }
}
