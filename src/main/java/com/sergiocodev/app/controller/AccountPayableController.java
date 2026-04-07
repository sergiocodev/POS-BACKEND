package com.sergiocodev.app.controller;

import com.sergiocodev.app.config.UserPrincipal;
import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.model.AccountPayable;
import com.sergiocodev.app.service.AccountPayableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-payables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Account Payables", description = "Endpoints para la gestión de cuentas por pagar a proveedores")
@SecurityRequirement(name = "bearerAuth")
public class AccountPayableController {

    private final AccountPayableService service;

    @GetMapping
    @Operation(summary = "Listar todas las cuentas por pagar")
    public ResponseEntity<ResponseApi<List<AccountPayableResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Obtener cuentas por pagar de un proveedor")
    public ResponseEntity<ResponseApi<List<AccountPayableResponse>>> getBySupplierId(@PathVariable Long supplierId) {
        return ResponseEntity.ok(ResponseApi.success(service.getBySupplierId(supplierId)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener cuentas por pagar por su estado (PENDING, PARTIAL, PAID)")
    public ResponseEntity<ResponseApi<List<AccountPayableResponse>>> getByStatus(
            @PathVariable AccountPayable.PayableStatus status) {
        return ResponseEntity.ok(ResponseApi.success(service.getByStatus(status)));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Registrar un pago (total o parcial) a una cuenta pendiente")
    public ResponseEntity<ResponseApi<AccountPayableResponse>> pay(
            @PathVariable Long id,
            @Valid @RequestBody AccountPayablePaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ResponseApi.success(service.pay(id, request, principal.getId()), "Pago registrado exitosamente"));
    }
}
