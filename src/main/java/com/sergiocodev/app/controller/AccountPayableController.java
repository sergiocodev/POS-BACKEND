package com.sergiocodev.app.controller;

import com.sergiocodev.app.config.UserPrincipal;
import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.model.AccountPayable;
import com.sergiocodev.app.service.interfaces.AccountPayableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.sergiocodev.app.dto.accountpayable.AccountPayableDashboardResponse;

@RestController
@RequestMapping("/api/v1/account-payables")
@RequiredArgsConstructor
@Tag(name = "Account Payables", description = "Endpoints para la gestión de cuentas por pagar a proveedores")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_CUENTAS_PAGAR + "')")
public class AccountPayableController {

    private final AccountPayableService service;

    @GetMapping
    @Operation(summary = "Listar todas las cuentas por pagar")
    public ResponseEntity<ResponseApi<List<AccountPayableResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/paged")
    public ResponseEntity<ResponseApi<Page<AccountPayableResponse>>> getAllPaged(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String purchaseIdentifier,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String dueDate,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<AccountPayableResponse> paged = service.getAllPaged(supplierName, purchaseIdentifier, createdAt, dueDate, status, pageable);
        return ResponseEntity.ok(ResponseApi.success(paged));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ResponseApi<List<AccountPayableDashboardResponse>>> getDashboard() {
        return ResponseEntity.ok(ResponseApi.success(service.getDashboard()));
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
