package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.company.CompanyRequest;
import com.sergiocodev.app.dto.company.CompanyResponse;
import com.sergiocodev.app.service.interfaces.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Endpoints para la gestión de la empresa")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<CompanyResponse> getCompany() {
        return ResponseEntity.ok(companyService.getCompany());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.CONFIGURACION_ESTABLECIMIENTOS + "')")
    public ResponseEntity<CompanyResponse> updateCompany(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(request));
    }

    @GetMapping("/is-configured")
    public ResponseEntity<Boolean> isConfigured() {
        return ResponseEntity.ok(companyService.isConfigured());
    }
}
