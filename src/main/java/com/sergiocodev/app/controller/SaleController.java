package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import com.sergiocodev.app.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Sales", description = "Endpoints para la gestión de ventas")
@SecurityRequirement(name = "bearerAuth")
public class SaleController {

    private final SaleService service;

    @PostMapping
    @Operation(summary = "Procesar una nueva venta")
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request, @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, userId));
    }

    @GetMapping
    @Operation(summary = "Listar todas las ventas")
    public ResponseEntity<List<SaleResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por ID")
    public ResponseEntity<SaleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una venta")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descargar PDF/Ticket")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        byte[] pdf = service.getPdf(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/{id}/xml")
    @Operation(summary = "Descargar XML")
    public ResponseEntity<String> getXml(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + ".xml")
                .body(service.getXml(id));
    }

    @GetMapping("/{id}/cdr")
    @Operation(summary = "Descargar CDR (Constancia SUNAT)")
    public ResponseEntity<String> getCdr(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + "-cdr.xml")
                .body(service.getCdr(id));
    }

    @PostMapping("/{id}/credit-note")
    @Operation(summary = "Emitir Nota de Crédito")
    public ResponseEntity<SaleResponse> createCreditNote(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long userId) {
        return ResponseEntity.ok(service.createCreditNote(id, reason, userId));
    }

    @PostMapping("/{id}/debit-note")
    @Operation(summary = "Emitir Nota de Débito")
    public ResponseEntity<SaleResponse> createDebitNote(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long userId) {
        return ResponseEntity.ok(service.createDebitNote(id, reason, userId));
    }

    @PostMapping("/{id}/invalidate")
    @Operation(summary = "Invalidar/Baja de documento")
    public ResponseEntity<Void> invalidate(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long userId) {
        service.invalidate(id, reason, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ListProductsForSale")
    @Operation(summary = "Listar produtos para venta con stock y detalles")
    public ResponseEntity<List<ProductForSaleResponse>> listProductsForSale(
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(service.listProductsForSale(establishmentId));
    }
}
