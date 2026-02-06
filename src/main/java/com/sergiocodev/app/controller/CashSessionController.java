package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.service.CashSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Cash Sessions", description = "Endpoints para la gestión de sesiones de efectivo")
@SecurityRequirement(name = "bearerAuth")
public class CashSessionController {

    private final CashSessionService service;

    @PostMapping("/open")
    @Operation(summary = "Abrir una nueva sesión de efectivo")
    public ResponseEntity<ResponseApi<CashSessionResponse>> openSession(@Valid @RequestBody CashSessionRequest request,
            @RequestParam Long userId) {
        return ResponseEntity
                .ok(ResponseApi.success(service.openSession(request, userId), "Sesión abierta exitosamente"));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Cerrar una sesión de efectivo activa")
    public ResponseEntity<ResponseApi<CashSessionResponse>> closeSession(@PathVariable Long id,
            @RequestParam BigDecimal closingBalance) {
        return ResponseEntity
                .ok(ResponseApi.success(service.closeSession(id, closingBalance), "Sesión cerrada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todas las sesiones de efectivo")
    public ResponseEntity<ResponseApi<List<CashSessionResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una sesión de efectivo por ID")
    public ResponseEntity<ResponseApi<CashSessionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @GetMapping("/active")
    @Operation(summary = "Obtener sesión de efectivo activa para el usuario")
    public ResponseEntity<ResponseApi<CashSessionResponse>> getActiveSession(@RequestParam Long userId) {
        return ResponseEntity.ok(ResponseApi.success(service.getActiveSession(userId)));
    }

    @GetMapping("/status")
    @Operation(summary = "Ver montos actuales del turno", description = "Muestra el estado de la caja y montos acumulados")
    public ResponseEntity<ResponseApi<CashSessionResponse>> getStatus(@RequestParam Long userId) {
        return ResponseEntity.ok(ResponseApi.success(service.getStatus(userId)));
    }

    @PostMapping("/close")
    @Operation(summary = "Arqueo y cierre de caja", description = "Cierra la sesión activa del usuario")
    public ResponseEntity<ResponseApi<CashSessionResponse>> closeActiveSession(@RequestParam Long userId,
            @RequestParam BigDecimal closingBalance) {
        return ResponseEntity.ok(
                ResponseApi.success(service.closeActiveSession(userId, closingBalance), "Caja cerrada exitosamente"));
    }

    @GetMapping("/history")
    @Operation(summary = "Historial de cierres", description = "Listado de sesiones cerradas del usuario")
    public ResponseEntity<ResponseApi<List<CashSessionResponse>>> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(ResponseApi.success(service.getHistory(userId)));
    }
}
