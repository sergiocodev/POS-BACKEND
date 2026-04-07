package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.company.CompanyRequest;
import com.sergiocodev.app.dto.company.CompanyResponse;
import com.sergiocodev.app.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<CompanyResponse> getCompany() {
        return ResponseEntity.ok(companyService.getCompany());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyResponse> updateCompany(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(request));
    }

    @GetMapping("/is-configured")
    public ResponseEntity<Boolean> isConfigured() {
        return ResponseEntity.ok(companyService.isConfigured());
    }
}
