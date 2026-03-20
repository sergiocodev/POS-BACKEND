package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentResponse;
import com.sergiocodev.app.service.AccountPayablePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-payable-payments")
@RequiredArgsConstructor
public class AccountPayablePaymentController {

    private final AccountPayablePaymentService service;

    @PostMapping
    public ResponseEntity<AccountPayablePaymentResponse> create(
            @Valid @RequestBody AccountPayablePaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // En una app real se extrae el userId del UserDetails, aquí simulamos con ID 1
        Long userId = 1L;
        return new ResponseEntity<>(service.create(request, userId), HttpStatus.CREATED);
    }

    @GetMapping("/payable/{accountPayableId}")
    public ResponseEntity<List<AccountPayablePaymentResponse>> getByAccountPayableId(
            @PathVariable Long accountPayableId) {
        return ResponseEntity.ok(service.getByAccountPayableId(accountPayableId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
