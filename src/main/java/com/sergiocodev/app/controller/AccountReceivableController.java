package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivableRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableResponse;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableDashboardResponse;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentResponse;
import com.sergiocodev.app.service.interfaces.AccountReceivableService;
import com.sergiocodev.app.service.interfaces.AccountReceivablePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.time.LocalDate;
import com.sergiocodev.app.dto.ResponseApi;

@RestController
@RequestMapping("/api/v1/account-receivables")
@RequiredArgsConstructor
@Tag(name = "Account Receivables", description = "Endpoints para la gestión de cuentas por cobrar")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.VENTAS_CUENTAS_COBRAR)
public class AccountReceivableController {

    private final AccountReceivableService service;
    private final AccountReceivablePaymentService paymentService;

    @PostMapping
    public ResponseEntity<AccountReceivableResponse> create(@Valid @RequestBody AccountReceivableRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AccountReceivableResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/paged")
    public ResponseEntity<ResponseApi<Page<AccountReceivableResponse>>> getAllPaged(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String saleIdentifier,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String dueDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = true) Long establishmentId,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<AccountReceivableResponse> paged = service.getAllPaged(customerName, saleIdentifier, createdAt, dueDate, status, establishmentId, pageable);
        return ResponseEntity.ok(ResponseApi.success(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountReceivableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<ResponseApi<List<AccountReceivablePaymentResponse>>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(paymentService.getByAccountReceivableId(id)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<AccountReceivableDashboardResponse>> getDashboard(@RequestParam(required = true) Long establishmentId) {
        return ResponseEntity.ok(service.getDashboard(establishmentId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountReceivableResponse>> getByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getByCustomerId(customerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
