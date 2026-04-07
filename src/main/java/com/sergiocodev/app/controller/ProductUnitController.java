package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.productunit.ProductUnitRequest;
import com.sergiocodev.app.dto.productunit.ProductUnitResponse;
import com.sergiocodev.app.service.ProductUnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product-units")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Product Units", description = "Endpoints para la gestión de unidades de producto")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.INVENTARIO_CATALOGO + "')")
public class ProductUnitController {

    private final ProductUnitService service;

    @PostMapping
    public ResponseEntity<ProductUnitResponse> create(@Valid @RequestBody ProductUnitRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductUnitResponse>> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(service.getByProductId(productId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductUnitResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductUnitResponse> getByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(service.getByBarcode(barcode));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductUnitResponse> update(@PathVariable Long id,
            @Valid @RequestBody ProductUnitRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
