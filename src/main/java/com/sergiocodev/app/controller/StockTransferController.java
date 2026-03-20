package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.stocktransfer.StockTransferRequest;
import com.sergiocodev.app.dto.stocktransfer.StockTransferResponse;
import com.sergiocodev.app.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService service;

    @PostMapping
    public ResponseEntity<StockTransferResponse> create(
            @Valid @RequestBody StockTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L; // User simulado
        return new ResponseEntity<>(service.create(request, userId), HttpStatus.CREATED);
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
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L; // User simulado
        return ResponseEntity.ok(service.dispatchTransfer(id, userId));
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<StockTransferResponse> receiveTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L; // User simulado
        return ResponseEntity.ok(service.receiveTransfer(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StockTransferResponse> cancelTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L; // User simulado
        return ResponseEntity.ok(service.cancelTransfer(id, userId));
    }
}
