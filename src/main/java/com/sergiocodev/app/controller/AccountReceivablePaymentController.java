package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentResponse;
import com.sergiocodev.app.service.AccountReceivablePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-receivable-payments")
@RequiredArgsConstructor
public class AccountReceivablePaymentController {

    private final AccountReceivablePaymentService service;

    @PostMapping
    public ResponseEntity<AccountReceivablePaymentResponse> create(
            @Valid @RequestBody AccountReceivablePaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // En una app real se extrae el userId del UserDetails, aquí simulamos con ID 1
        Long userId = 1L;
        return new ResponseEntity<>(service.create(request, userId), HttpStatus.CREATED);
    }

    @GetMapping("/receivable/{accountReceivableId}")
    public ResponseEntity<List<AccountReceivablePaymentResponse>> getByAccountReceivableId(
            @PathVariable Long accountReceivableId) {
        return ResponseEntity.ok(service.getByAccountReceivableId(accountReceivableId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
