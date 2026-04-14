package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.repository.CashConceptRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-concepts")
@RequiredArgsConstructor
@Tag(name = "Cash Concepts", description = "Endpoints para la gestión de conceptos de caja")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionConstants.CAJA_MOVIMIENTOS + "')")
public class CashConceptController {

    private final CashConceptRepository repository;

    @GetMapping
    @Operation(summary = "Listar todos los conceptos de caja")
    public ResponseEntity<ResponseApi<List<CashConcept>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(repository.findAll()));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Filtrar conceptos por tipo (IN/OUT)")
    public ResponseEntity<ResponseApi<List<CashConcept>>> getByType(@PathVariable CashConcept.ConceptType type) {
        return ResponseEntity.ok(ResponseApi.success(repository.findByType(type)));
    }
}
