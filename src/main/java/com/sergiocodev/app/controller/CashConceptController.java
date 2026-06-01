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
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-concepts")
@RequiredArgsConstructor
@Tag(name = "Cash Concepts", description = "Endpoints para la gestión de conceptos de caja")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.CAJA_MOVIMIENTOS)
public class CashConceptController {

    private final CashConceptRepository repository;

    @GetMapping
    @Operation(summary = "Listar todos los conceptos de caja")
    public ResponseEntity<ResponseApi<List<CashConcept>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(repository.findAll()));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Filtrar conceptos por tipo (IN/OUT)")
    public ResponseEntity<ResponseApi<List<CashConcept>>> getByType(
            @PathVariable CashConcept.ConceptType type,
            @RequestParam(required = false) Boolean isSystem) {
        if (isSystem != null) {
            return ResponseEntity.ok(ResponseApi.success(repository.findByTypeAndIsSystem(type, isSystem)));
        }
        return ResponseEntity.ok(ResponseApi.success(repository.findByType(type)));
    }

    @PostMapping
    @Operation(summary = "Crear un concepto manual de caja")
    public ResponseEntity<ResponseApi<CashConcept>> create(@RequestBody com.sergiocodev.app.dto.CashConceptRequest request) {
        CashConcept concept = new CashConcept();
        concept.setName(request.name().toUpperCase());
        concept.setType(request.type());
        concept.setIsSystem(false); // Manually created concepts are never system concepts
        return ResponseEntity.ok(ResponseApi.success(repository.save(concept)));
    }
}
