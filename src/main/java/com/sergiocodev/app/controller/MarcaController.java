package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.marca.MarcaRequest;
import com.sergiocodev.app.dto.marca.MarcaResponse;
import com.sergiocodev.app.service.MarcaService;
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

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Brands", description = "Endpoints para gestión de marcas")
@SecurityRequirement(name = "bearerAuth")
public class MarcaController {

    private final MarcaService marcaService;

    @Operation(summary = "Crear marca", description = "Crea una nueva marca en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Marca creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Nombre de marca duplicado")
    })
    @PostMapping
    public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar marcas", description = "Obtiene la lista de todas las marcas registradas")
    @ApiResponse(responseCode = "200", description = "Lista de marcas obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<MarcaResponse>> obtenerTodas() {
        List<MarcaResponse> marcas = marcaService.obtenerTodas();
        return ResponseEntity.ok(marcas);
    }

    @Operation(summary = "Obtener marca por ID", description = "Obtiene una marca específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Marca encontrada"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponse> obtenerPorId(@PathVariable Long id) {
        MarcaResponse response = marcaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar marca", description = "Actualiza los datos de una marca existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Marca actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada"),
            @ApiResponse(responseCode = "409", description = "Nombre de marca duplicado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar marca", description = "Elimina una marca del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Marca eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        marcaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
