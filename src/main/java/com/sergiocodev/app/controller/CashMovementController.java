package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.cashmovement.CashMovementRequest;
import com.sergiocodev.app.dto.cashmovement.CashMovementResponse;
import com.sergiocodev.app.service.CashMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-movements")
@RequiredArgsConstructor
@Tag(name = "Cash Movements", description = "Endpoints para la gestión de movimientos de caja")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.CAJA_MOVIMIENTOS + "')")
public class CashMovementController {

    private final CashMovementService service;

    @GetMapping
    @Operation(summary = "Listar todos los movimientos", description = "Retorna una lista paginada de todos los movimientos de caja.")
    public ResponseEntity<ResponseApi<Page<CashMovementResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.findAll(pageable)));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Listar movimientos por sesión", description = "Retorna todos los movimientos asociados a una sesión de caja.")
    public ResponseEntity<ResponseApi<List<CashMovementResponse>>> getBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ResponseApi.success(service.findBySessionId(sessionId)));
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento manual", description = "Permite registrar un ingreso o egreso manual en la caja activa.")
    public ResponseEntity<ResponseApi<CashMovementResponse>> create(@Valid @RequestBody CashMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.createManualMovement(request), "Movimiento registrado exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento por ID")
    public ResponseEntity<ResponseApi<CashMovementResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar (cancelar) movimiento")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Movimiento eliminado exitosamente"));
    }
}
