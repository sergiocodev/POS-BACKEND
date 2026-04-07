package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivableRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableResponse;
import com.sergiocodev.app.service.AccountReceivableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account-receivables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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

    @GetMapping("/{id}")
    public ResponseEntity<AccountReceivableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
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
