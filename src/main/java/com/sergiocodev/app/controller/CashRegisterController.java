package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
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
@RequestMapping("/api/v1/cash-registers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Cash Registers", description = "Endpoints para la gestión de la caja registradora")
@SecurityRequirement(name = "bearerAuth")
public class CashRegisterController {

    private final CashRegisterService service;

    @PostMapping
    @Operation(summary = "Crear caja registradora")
    public ResponseEntity<ResponseApi<CashRegisterResponse>> create(@Valid @RequestBody CashRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Caja registradora creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todas las cajas registradoras")
    public ResponseEntity<ResponseApi<List<CashRegisterResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener caja registradora por ID")
    public ResponseEntity<ResponseApi<CashRegisterResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar caja registradora")
    public ResponseEntity<ResponseApi<CashRegisterResponse>> update(@PathVariable Long id,
            @Valid @RequestBody CashRegisterRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Caja registradora actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar caja registradora")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Caja registradora eliminada exitosamente"));
    }
}
