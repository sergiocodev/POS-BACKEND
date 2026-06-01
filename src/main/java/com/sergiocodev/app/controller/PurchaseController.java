package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.purchase.PurchaseRequest;
import com.sergiocodev.app.dto.purchase.PurchaseResponse;
import com.sergiocodev.app.service.interfaces.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Endpoints para la gestión de compras")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseController {

    private final PurchaseService service;

    @PostMapping
    @Operation(summary = "Procesar una nueva compra")
    @RequiresPermission(PermissionConstants.COMPRAS_NUEVA)
    public ResponseEntity<ResponseApi<PurchaseResponse>> create(@Valid @RequestBody PurchaseRequest request,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request, userId), "Compra procesada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todas las compras")
    @RequiresPermission(PermissionConstants.COMPRAS_LISTA)
    public ResponseEntity<ResponseApi<List<PurchaseResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar compras con paginación")
    @RequiresPermission(PermissionConstants.COMPRAS_LISTA)
    public ResponseEntity<ResponseApi<Page<PurchaseResponse>>> getAllPaged(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String supplierDocument,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String total,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String columnDate,
            @PageableDefault(size = 50, sort = "issueDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity
                .ok(ResponseApi.success(service.getAllPaged(startDate, endDate, documentType, series, number,
                        supplierName, supplierDocument, userName, status, total, paymentMethod,
                        columnDate, pageable)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener resumen de totales filtrados")
    @RequiresPermission(PermissionConstants.COMPRAS_LISTA)
    public ResponseEntity<ResponseApi<com.sergiocodev.app.dto.purchase.PurchaseSummaryResponse>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String supplierDocument,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String total,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String columnDate) {

        return ResponseEntity.ok(ResponseApi.success(service.getSummary(startDate, endDate, documentType, series,
                number,
                supplierName, supplierDocument, userName, status, total, paymentMethod, columnDate)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener compra por ID")
    @RequiresPermission(PermissionConstants.COMPRAS_LISTA)
    public ResponseEntity<ResponseApi<PurchaseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una compra")
    @RequiresPermission(PermissionConstants.COMPRAS_NUEVA)
    public ResponseEntity<ResponseApi<Void>> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Compra cancelada exitosamente"));
    }
}
