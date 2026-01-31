package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.cashregister.CashRegisterRequest;
import com.sergiocodev.app.dto.cashregister.CashRegisterResponse;
import com.sergiocodev.app.service.CashRegisterService;
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
@RequestMapping("/api/cash-registers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Cash Registers", description = "Endpoints para la gestión de la caja registradora")
@SecurityRequirement(name = "bearerAuth")
public class CashRegisterController {

    private final CashRegisterService service;

    @PostMapping
    @Operation(summary = "Crear caja registradora")
    public ResponseEntity<CashRegisterResponse> create(@Valid @RequestBody CashRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las cajas registradoras")
    public ResponseEntity<List<CashRegisterResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener caja registradora por ID")
    public ResponseEntity<CashRegisterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar caja registradora")
    public ResponseEntity<CashRegisterResponse> update(@PathVariable Long id,
            @Valid @RequestBody CashRegisterRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar caja registradora")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
