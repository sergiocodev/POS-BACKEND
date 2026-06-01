package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.stockmovement.StockMovementRequest;
import com.sergiocodev.app.dto.stockmovement.StockMovementResponse;
import com.sergiocodev.app.service.interfaces.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
@Tag(name = "Stock Movements", description = "Endpoints para la gestión de movimientos de stock")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.INVENTARIO_MOVIMIENTOS + "')")
public class StockMovementController {

    private final StockMovementService service;

    @PostMapping
    @Operation(summary = "Crear movimiento manual de stock")
    public ResponseEntity<ResponseApi<StockMovementResponse>> create(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Movimiento de stock registrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todos los movimientos de stock")
    public ResponseEntity<ResponseApi<List<StockMovementResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Listar movimientos por producto")
    public ResponseEntity<ResponseApi<List<StockMovementResponse>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ResponseApi.success(service.getByProduct(productId)));
    }

    @GetMapping("/establishment/{establishmentId}")
    @Operation(summary = "Listar movimientos de stock por establecimiento")
    public ResponseEntity<ResponseApi<List<StockMovementResponse>>> getByEstablishment(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getByEstablishment(establishmentId)));
    }

    @GetMapping("/establishment/{establishmentId}/paged")
    @Operation(summary = "Listar movimientos de stock por establecimiento con paginación")
    public ResponseEntity<ResponseApi<org.springframework.data.domain.Page<StockMovementResponse>>> getByEstablishmentPaged(
            @PathVariable Long establishmentId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String lotCode,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String userName,
            @org.springframework.data.web.PageableDefault(size = 50, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.getByEstablishmentPaged(establishmentId, productName, lotCode, type, reason, userName, pageable)));
    }
}
