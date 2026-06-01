package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.cash.CashInflowRequest;
import com.sergiocodev.app.dto.cash.CashOutflowRequest;
import com.sergiocodev.app.dto.cash.CloseSessionRequest;
import com.sergiocodev.app.dto.cash.OpenDailySessionRequest;
import com.sergiocodev.app.dto.cash.SessionStatusResponse;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.dto.cashmovement.CashMovementResponse;
import com.sergiocodev.app.service.interfaces.CashSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
@Tag(name = "Cash Management", description = "Endpoints para la gestión de caja (Tesorería)")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.CAJA_APERTURA_CIERRE)
public class CashController {

    private final CashSessionService service;

    @PostMapping("/OpenDailySession")
    @Operation(summary = "Inicia el día operativo", description = "Crea registro en cash_sessions y valida que no haya otra abierta.")
    public ResponseEntity<ResponseApi<CashSessionResponse>> openDailySession(
            @Valid @RequestBody OpenDailySessionRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.openDailySession(request), "Sesión iniciada exitosamente"));
    }

    @GetMapping("/GetCurrentSessionStatus")
    @Operation(summary = "Arqueo de caja rápido", description = "Devuelve el Saldo Teórico vs Saldo Inicial.")
    public ResponseEntity<ResponseApi<SessionStatusResponse>> getCurrentSessionStatus(
            @RequestParam Long userId) {
        return ResponseEntity.ok(ResponseApi.success(service.getCurrentSessionStatus(userId)));
    }

    @PostMapping("/RegisterCashOutflow")
    @Operation(summary = "Retiro de dinero", description = "Registra un egreso de caja.")
    public ResponseEntity<ResponseApi<CashMovementResponse>> registerCashOutflow(
            @Valid @RequestBody CashOutflowRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.registerCashOutflow(request), "Egreso registrado exitosamente"));
    }

    @PostMapping("/RegisterCashInflow")
    @Operation(summary = "Ingreso de dinero", description = "Registra un ingreso a caja que no provenga de una venta directa (ej. saldo rotativo).")
    public ResponseEntity<ResponseApi<CashMovementResponse>> registerCashInflow(
            @Valid @RequestBody CashInflowRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.registerCashInflow(request), "Ingreso registrado exitosamente"));
    }

    @PostMapping("/CloseSessionAndReport")
    @Operation(summary = "Cierre de turno", description = "Cierra la sesión y calcula diferencias.")
    public ResponseEntity<ResponseApi<CashSessionResponse>> closeSessionAndReport(
            @Valid @RequestBody CloseSessionRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.closeSessionAndReport(request), "Sesión cerrada exitosamente"));
    }
}
