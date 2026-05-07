package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivableRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableResponse;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableDashboardResponse;
import com.sergiocodev.app.service.AccountReceivableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_CUENTAS_COBRAR + "')")
public class AccountReceivableController {

    private final AccountReceivableService service;

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
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<AccountReceivableResponse> paged = service.getAllPaged(customerName, saleIdentifier, createdAt, dueDate, status, pageable);
        return ResponseEntity.ok(ResponseApi.success(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountReceivableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<AccountReceivableDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(service.getDashboard());
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
