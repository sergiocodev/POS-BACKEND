package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormRequest;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormResponse;
import com.sergiocodev.app.service.PharmaceuticalFormService;
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
@RequestMapping("/api/v1/pharmaceutical-forms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Pharmaceutical Forms", description = "Endpoints for managing pharmaceutical forms")
@SecurityRequirement(name = "bearerAuth")
public class PharmaceuticalFormController {

    private final PharmaceuticalFormService pharmaceuticalFormService;

    @Operation(summary = "List all pharmaceutical forms")
    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<List<PharmaceuticalFormResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(pharmaceuticalFormService.findAll()));
    }

    @Operation(summary = "Get pharmaceutical form by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<PharmaceuticalFormResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(pharmaceuticalFormService.findById(id)));
    }

    @Operation(summary = "Create new pharmaceutical form")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<PharmaceuticalFormResponse>> create(
            @Valid @RequestBody PharmaceuticalFormRequest request) {
        PharmaceuticalFormResponse created = pharmaceuticalFormService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(created, "Pharmaceutical form created successfully"));
    }

    @Operation(summary = "Update pharmaceutical form")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<PharmaceuticalFormResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PharmaceuticalFormRequest request) {
        return ResponseEntity.ok(ResponseApi.success(pharmaceuticalFormService.update(id, request),
                "Pharmaceutical form updated successfully"));
    }

    @Operation(summary = "Delete pharmaceutical form")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FARMACIA + "')")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        pharmaceuticalFormService.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Pharmaceutical form deleted successfully"));
    }
}
