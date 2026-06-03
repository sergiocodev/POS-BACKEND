package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.brand.BrandRequest;
import com.sergiocodev.app.dto.brand.BrandResponse;
import com.sergiocodev.app.service.interfaces.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Endpoints para la gestión de marcas")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.FARMACIA_MARCAS)
public class BrandController {

    private final BrandService brandService;

    @Operation(summary = "Crear marca", description = "Crea una nueva marca en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Brand created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Duplicate brand name")
    })
    @PostMapping
    public ResponseEntity<ResponseApi<BrandResponse>> createNewBrand(@Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.createNewBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(response, "Marca creada exitosamente"));
    }

    @Operation(summary = "Listar marcas", description = "Obtiene la lista de todas las marcas registradas")
    @ApiResponse(responseCode = "200", description = "List of brands obtained successfully")
    @GetMapping
    public ResponseEntity<ResponseApi<List<BrandResponse>>> getAllBrands() {
        List<BrandResponse> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ResponseApi.success(brands));
    }

    @Operation(summary = "Listar marcas paginadas", description = "Retorna una lista paginada de todas las marcas.")
    @GetMapping("/paged")
    public ResponseEntity<ResponseApi<Page<BrandResponse>>> getPaged(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(brandService.findAllPaged(name, pageable)));
    }

    @Operation(summary = "Obtener marca por ID", description = "Obtiene una marca específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand found"),
            @ApiResponse(responseCode = "404", description = "Brand not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<BrandResponse>> getBrandById(@PathVariable Long id) {
        BrandResponse response = brandService.getBrandById(id);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    @Operation(summary = "Actualizar marca", description = "Actualiza los datos de una marca existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate brand name")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<BrandResponse>> updateBrandById(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.updateBrandById(id, request);
        return ResponseEntity.ok(ResponseApi.success(response, "Marca actualizada exitosamente"));
    }

    @Operation(summary = "Eliminar marca", description = "Elimina una marca del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseApi<Void>> deleteBrandById(@PathVariable Long id) {
        brandService.deleteBrandById(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Marca eliminada exitosamente"));
    }
}
