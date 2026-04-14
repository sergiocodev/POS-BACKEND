package com.sergiocodev.app.controller;

import com.sergiocodev.app.config.UserPrincipal;
import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentResponse;
import com.sergiocodev.app.service.AccountPayablePaymentService;
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
@RequestMapping("/api/v1/account-payable-payments")
@RequiredArgsConstructor
@Tag(name = "Account Payable Payments", description = "Endpoints para la gestión de pagos de cuentas por pagar")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_CUENTAS_PAGAR + "')")
public class AccountPayablePaymentController {

    private final AccountPayablePaymentService service;

    @PostMapping
    public ResponseEntity<ResponseApi<AccountPayablePaymentResponse>> create(
            @Valid @RequestBody AccountPayablePaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(
                ResponseApi.success(service.create(request, principal.getId()), "Pago registrado exitosamente"),
                HttpStatus.CREATED);
    }

    @GetMapping("/payable/{accountPayableId}")
    public ResponseEntity<ResponseApi<List<AccountPayablePaymentResponse>>> getByAccountPayableId(
            @PathVariable Long accountPayableId) {
        return ResponseEntity.ok(ResponseApi.success(service.getByAccountPayableId(accountPayableId)));
    }

    @GetMapping("/history")
    @Operation(summary = "Obtener historial de pagos con filtros y paginación")
    public ResponseEntity<ResponseApi<Page<AccountPayablePaymentResponse>>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String paymentMethod,
            Pageable pageable) {
        return ResponseEntity
                .ok(ResponseApi.success(service.getHistory(startDate, endDate, supplierId, paymentMethod, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
