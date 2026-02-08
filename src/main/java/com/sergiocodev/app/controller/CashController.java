package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.cash.CashOutflowRequest;
import com.sergiocodev.app.dto.cash.CloseSessionRequest;
import com.sergiocodev.app.dto.cash.OpenDailySessionRequest;
import com.sergiocodev.app.dto.cash.SessionStatusResponse;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.service.CashSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Cash Management", description = "Endpoints para la gestión de caja (Tesorería)")
@SecurityRequirement(name = "bearerAuth")
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
    public ResponseEntity<ResponseApi<CashMovement>> registerCashOutflow(
            @Valid @RequestBody CashOutflowRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.registerCashOutflow(request), "Egreso registrado exitosamente"));
    }

    @PostMapping("/CloseSessionAndReport")
    @Operation(summary = "Cierre de turno", description = "Cierra la sesión y calcula diferencias.")
    public ResponseEntity<ResponseApi<CashSessionResponse>> closeSessionAndReport(
            @Valid @RequestBody CloseSessionRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.closeSessionAndReport(request), "Sesión cerrada exitosamente"));
    }
}
