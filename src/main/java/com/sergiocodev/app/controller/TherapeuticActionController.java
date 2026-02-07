package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionRequest;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionResponse;
import com.sergiocodev.app.service.TherapeuticActionService;
import com.sergiocodev.app.util.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/therapeutic-actions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Therapeutic Actions", description = "Endpoints for managing therapeutic actions")
@SecurityRequirement(name = "bearerAuth")
public class TherapeuticActionController {

    private final TherapeuticActionService service;

    @Operation(summary = "List all therapeutic actions")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<List<TherapeuticActionResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.findAll()));
    }

    @Operation(summary = "Get therapeutic action by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<TherapeuticActionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.findById(id)));
    }

    @Operation(summary = "Create new therapeutic action")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<TherapeuticActionResponse>> create(
            @Valid @RequestBody TherapeuticActionRequest request) {
        TherapeuticActionResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(created, "Therapeutic action created successfully"));
    }

    @Operation(summary = "Update therapeutic action")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<TherapeuticActionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TherapeuticActionRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.update(id, request),
                "Therapeutic action updated successfully"));
    }

    @Operation(summary = "Delete therapeutic action")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Therapeutic action deleted successfully"));
    }
}
