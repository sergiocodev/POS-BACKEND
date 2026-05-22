package com.sergiocodev.app.controller;

import com.sergiocodev.app.config.UserPrincipal;
import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentResponse;
import com.sergiocodev.app.service.interfaces.AccountReceivablePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account-receivable-payments")
@RequiredArgsConstructor
@Tag(name = "Account Receivable Payments", description = "Endpoints para la gestión de pagos de cuentas por cobrar")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_CUENTAS_COBRAR + "')")
public class AccountReceivablePaymentController {

    private final AccountReceivablePaymentService service;

    @PostMapping
    public ResponseEntity<ResponseApi<AccountReceivablePaymentResponse>> create(
            @Valid @RequestBody AccountReceivablePaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(
                ResponseApi.success(service.create(request, principal.getId()), "Cobro registrado exitosamente"),
                HttpStatus.CREATED);
    }

    @GetMapping("/receivable/{accountReceivableId}")
    public ResponseEntity<ResponseApi<List<AccountReceivablePaymentResponse>>> getByAccountReceivableId(
            @PathVariable Long accountReceivableId) {
        return ResponseEntity.ok(ResponseApi.success(service.getByAccountReceivableId(accountReceivableId)));
    }

    @GetMapping("/history")
    @Operation(summary = "Obtener historial de cobros con filtros y paginación")
    public ResponseEntity<ResponseApi<Page<AccountReceivablePaymentResponse>>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String paymentMethod,
            Pageable pageable) {
        return ResponseEntity
                .ok(ResponseApi.success(service.getHistory(startDate, endDate, customerId, paymentMethod, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
