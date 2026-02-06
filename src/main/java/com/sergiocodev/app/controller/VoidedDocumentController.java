package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentRequest;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentResponse;
import com.sergiocodev.app.model.VoidedDocument;
import com.sergiocodev.app.service.VoidedDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/voided-documents")
@RequiredArgsConstructor
@Tag(name = "Voided Documents", description = "API para gestión de bajas de documentos SUNAT")
public class VoidedDocumentController {

    private final VoidedDocumentService service;

    @PostMapping
    @Operation(summary = "Crear baja de documentos")
    public ResponseEntity<ResponseApi<VoidedDocumentResponse>> create(
            @Valid @RequestBody VoidedDocumentRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request, userId), "Baja de documentos creada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todas las bajas")
    public ResponseEntity<ResponseApi<List<VoidedDocumentResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener baja por ID")
    public ResponseEntity<ResponseApi<VoidedDocumentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @GetMapping("/establishment/{establishmentId}")
    @Operation(summary = "Listar bajas por establecimiento")
    public ResponseEntity<ResponseApi<List<VoidedDocumentResponse>>> getByEstablishment(
            @PathVariable Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getByEstablishment(establishmentId)));
    }

    @PatchMapping("/{id}/sunat-status")
    @Operation(summary = "Actualizar estado SUNAT de la baja")
    public ResponseEntity<ResponseApi<VoidedDocumentResponse>> updateSunatStatus(
            @PathVariable Long id,
            @RequestParam VoidedDocument.VoidedSunatStatus status,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(ResponseApi.success(service.updateSunatStatus(id, status, description),
                "Estado SUNAT actualizado exitosamente"));
    }

    @PostMapping("/process")
    @Operation(summary = "Proceso Batch: Enviar bajas del día a SUNAT")
    public ResponseEntity<ResponseApi<Void>> processDailyVoids(@RequestParam Long establishmentId) {
        service.processDailyVoids(establishmentId);
        return ResponseEntity.ok(ResponseApi.success(null, "Bajas enviadas a SUNAT exitosamente"));
    }
}
