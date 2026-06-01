package com.sergiocodev.app.controller;

import com.sergiocodev.app.config.UserPrincipal;
import com.sergiocodev.app.dto.stocktransfer.StockTransferRequest;
import com.sergiocodev.app.dto.stocktransfer.StockTransferResponse;
import com.sergiocodev.app.service.interfaces.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-transfers")
@RequiredArgsConstructor
@Tag(name = "Stock Transfers", description = "Endpoints para la gestión de transferencias de stock")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.INVENTARIO_TRANSFERENCIAS)
public class StockTransferController {

    private final StockTransferService service;

    @PostMapping
    public ResponseEntity<StockTransferResponse> create(
            @Valid @RequestBody StockTransferRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(service.create(request, principal.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/source/{establishmentId}")
    public ResponseEntity<List<StockTransferResponse>> getBySourceEstablishmentId(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(service.getBySourceEstablishmentId(establishmentId));
    }

    @GetMapping("/target/{establishmentId}")
    public ResponseEntity<List<StockTransferResponse>> getByTargetEstablishmentId(@PathVariable Long establishmentId) {
        return ResponseEntity.ok(service.getByTargetEstablishmentId(establishmentId));
    }

    @PutMapping("/{id}/dispatch")
    public ResponseEntity<StockTransferResponse> dispatchTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(service.dispatchTransfer(id, principal.getId()));
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<StockTransferResponse> receiveTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(service.receiveTransfer(id, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StockTransferResponse> cancelTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(service.cancelTransfer(id, principal.getId()));
    }
}
