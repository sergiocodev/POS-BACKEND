package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import com.sergiocodev.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.sergiocodev.app.dto.productlot.ProductLotResponse;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "Endpoints para la gestión de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService service;

    @PostMapping
    @Operation(summary = "Crear producto")
    public ResponseEntity<ResponseApi<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Producto creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar productos con filtros", description = "Obtiene la lista de productos, opcionalmente filtrada por categoría, marca o estado")
    public ResponseEntity<ResponseApi<List<ProductResponse>>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(ResponseApi.success(service.getAll(categoryId, brandId, active)));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar productos (POS)", description = "Busca productos por código de barras, nombre o principio activo")
    public ResponseEntity<ResponseApi<List<ProductResponse>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ResponseApi.success(service.search(query)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ResponseApi<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ResponseApi<ProductResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.update(id, request), "Producto actualizado exitosamente"));
    }

    @GetMapping("/{id}/lots")
    @Operation(summary = "Ver lotes asociados", description = "Obtiene los lotes asociados a un producto")
    public ResponseEntity<ResponseApi<List<ProductLotResponse>>> getLots(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getLots(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Producto eliminado exitosamente"));
    }
}
