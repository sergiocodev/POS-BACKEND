package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.inventory.InventoryRequest;
import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.service.interfaces.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.sergiocodev.app.util.PermissionConstants;
import com.sergiocodev.app.config.ApiVersion;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Endpoints para la gestión de inventario")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.INVENTARIO_ACTUAL + "')")
@ApiVersion(1)
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/update")
    @Operation(summary = "Actualizar nivel de stock manualmente")
    public ResponseEntity<ResponseApi<InventoryResponse>> updateStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.updateStock(request), "Stock actualizado exitosamente"));
    }

    @PostMapping("/adjustments")
    @Operation(summary = "Ajuste manual de inventario (Robo/Pérdida/Ingreso)")
    public ResponseEntity<ResponseApi<InventoryResponse>> adjustStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.updateStock(request), "Ajuste de inventario procesado exitosamente"));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Reporte Vencimientos (Semáforo)", description = "Lista productos próximos a vencer (90 días)")
    public ResponseEntity<ResponseApi<List<InventoryResponse>>> getAlerts() {
        return ResponseEntity.ok(ResponseApi.success(service.getAlerts()));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Reporte stock mínimo", description = "Lista productos con stock bajo (<= 10)")
    public ResponseEntity<ResponseApi<List<InventoryResponse>>> getLowStock() {
        return ResponseEntity.ok(ResponseApi.success(service.getLowStock()));
    }

    @GetMapping
    @Operation(summary = "Listar todos los registros de inventario")
    public ResponseEntity<ResponseApi<List<InventoryResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/establishment/{establishmentId}/paged")
    @Operation(summary = "Listar inventario por establecimiento con paginación")
    public ResponseEntity<ResponseApi<Page<InventoryResponse>>> getByEstablishmentPaged(
            @PathVariable Long establishmentId,
            @PageableDefault(size = 50, sort = "quantity") Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.getByEstablishmentPaged(establishmentId, pageable)));
    }

    @GetMapping("/establishment/{establishmentId}")
    @Operation(summary = "Listar inventario por establecimiento")
    public ResponseEntity<ResponseApi<List<InventoryResponse>>> getByEstablishment(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getByEstablishment(establishmentId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de inventario por ID")
    public ResponseEntity<ResponseApi<InventoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @GetMapping("/alerts/low-stock/paged")
    @Operation(summary = "Reporte stock mínimo con paginación")
    public ResponseEntity<ResponseApi<List<com.sergiocodev.app.dto.inventory.LowStockAlertResponse>>> getLowStockAlertsPaged() {
        return ResponseEntity.ok(ResponseApi.success(service.getLowStockAlerts()));
    }

    @GetMapping("/GetLowStockAlerts")
    @Operation(summary = "Reporte de reposición", description = "Retorna la lista de productos que necesitan compra urgente (quantity <= min_stock)")
    public ResponseEntity<ResponseApi<List<com.sergiocodev.app.dto.inventory.LowStockAlertResponse>>> getLowStockAlerts() {
        return ResponseEntity.ok(ResponseApi.success(service.getLowStockAlerts()));
    }

    @GetMapping("/expiring/paged")
    @Operation(summary = "Prevención de pérdidas con paginación")
    public ResponseEntity<ResponseApi<List<com.sergiocodev.app.dto.inventory.ExpiringLotResponse>>> getExpiringLotsPaged(
            @RequestParam(required = false, defaultValue = "90") Integer days) {
        return ResponseEntity.ok(ResponseApi.success(service.getExpiringLots(days)));
    }

    @GetMapping("/GetExpiringLots")
    @Operation(summary = "Prevención de pérdidas por vencimiento", description = "Filtra fechas de expiry_date que estén dentro de los próximos 30, 60 o 90 días")
    public ResponseEntity<ResponseApi<List<com.sergiocodev.app.dto.inventory.ExpiringLotResponse>>> getExpiringLots(
            @RequestParam(required = false, defaultValue = "90") Integer days) {
        return ResponseEntity.ok(ResponseApi.success(service.getExpiringLots(days)));
    }

    @PostMapping("/RegisterStockAdjustment")
    @Operation(summary = "Corrige el stock manual", description = "Corrige el stock manual (por robo, rotura o error de conteo)")
    public ResponseEntity<ResponseApi<InventoryResponse>> registerStockAdjustment(
            @Valid @RequestBody com.sergiocodev.app.dto.inventory.StockAdjustmentRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.registerStockAdjustment(request),
                "Ajuste de stock registrado exitosamente"));
    }

    @GetMapping("/GetKardexHistoryByProduct")
    @Operation(summary = "Auditoría de un producto", description = "Muestra la trazabilidad completa filtrando por product_id")
    public ResponseEntity<ResponseApi<List<com.sergiocodev.app.dto.inventory.KardexHistoryResponse>>> getKardexHistoryByProduct(
            @RequestParam Long productId) {
        return ResponseEntity.ok(ResponseApi.success(service.getKardexHistoryByProduct(productId)));
    }
}
