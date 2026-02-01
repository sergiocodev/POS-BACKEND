package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.kardex.KardexResponse;
import com.sergiocodev.app.service.KardexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kardex")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Kardex", description = "Endpoints para el histórico de movimientos")
@SecurityRequirement(name = "bearerAuth")
public class KardexController {

    private final KardexService service;

    @GetMapping("/{productId}")
    @Operation(summary = "Ver movimientos históricos de un producto")
    public ResponseEntity<List<KardexResponse>> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(service.getByProductId(productId));
    }
}
