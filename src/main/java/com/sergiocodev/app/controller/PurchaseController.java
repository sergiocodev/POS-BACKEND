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
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Endpoints para la gestión de compras")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseController {

    private final PurchaseService service;

    @PostMapping
    @Operation(summary = "Procesar una nueva compra")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_NUEVA + "')")
    public ResponseEntity<ResponseApi<PurchaseResponse>> create(@Valid @RequestBody PurchaseRequest request,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request, userId), "Compra procesada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todas las compras")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_LISTA + "')")
    public ResponseEntity<ResponseApi<List<PurchaseResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener compra por ID")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_LISTA + "')")
    public ResponseEntity<ResponseApi<PurchaseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una compra")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_NUEVA + "')")
    public ResponseEntity<ResponseApi<Void>> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Compra cancelada exitosamente"));
    }
}
