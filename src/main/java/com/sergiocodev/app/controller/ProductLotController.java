package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.productlot.ProductLotRequest;
import com.sergiocodev.app.dto.productlot.ProductLotResponse;
import com.sergiocodev.app.service.ProductLotService;
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
@RequestMapping("/api/product-lots")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Product Lots", description = "Endpoints para la gestión de lotes de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductLotController {

    private final ProductLotService service;

    @PostMapping
    @Operation(summary = "Crear lote de producto")
    public ResponseEntity<ProductLotResponse> create(@Valid @RequestBody ProductLotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos los lotes de productos")
    public ResponseEntity<List<ProductLotResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Listado de lotes de productos por ID")
    public ResponseEntity<List<ProductLotResponse>> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(service.getByProductId(productId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote de producto por ID del producto")
    public ResponseEntity<ProductLotResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lote de producto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
