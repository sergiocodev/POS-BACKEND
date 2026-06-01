package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.presentation.PresentationRequest;
import com.sergiocodev.app.dto.presentation.PresentationResponse;
import com.sergiocodev.app.service.interfaces.PresentationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/presentations")
@RequiredArgsConstructor
@Tag(name = "Presentations", description = "Endpoints para la gestión de presentaciones")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.FARMACIA_PRESENTACIONES)
public class PresentationController {

    private final PresentationService service;

    @PostMapping("/CreateNewPresentation")
    @Operation(summary = "Crear presentación")
    public ResponseEntity<ResponseApi<PresentationResponse>> create(@Valid @RequestBody PresentationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Presentación creada exitosamente"));
    }

    @GetMapping("/GetAllPresentations")
    @Operation(summary = "Listar presentaciones")
    public ResponseEntity<ResponseApi<List<PresentationResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar presentaciones paginadas")
    public ResponseEntity<ResponseApi<Page<PresentationResponse>>> getPaged(
            @RequestParam(required = false) String description,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.findAllPaged(description, pageable)));
    }

    @GetMapping("/GetPresentationById/{id}")
    @Operation(summary = "Obtener presentación por ID")
    public ResponseEntity<ResponseApi<PresentationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/UpdatePresentationById/{id}")
    @Operation(summary = "Actualizar presentación")
    public ResponseEntity<ResponseApi<PresentationResponse>> update(@PathVariable Long id,
            @Valid @RequestBody PresentationRequest request) {
        return ResponseEntity
                .ok(ResponseApi.success(service.update(id, request), "Presentación actualizada exitosamente"));
    }

    @DeleteMapping("/DeletePresentationById/{id}")
    @Operation(summary = "Eliminar presentación")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Presentación eliminada exitosamente"));
    }
}
