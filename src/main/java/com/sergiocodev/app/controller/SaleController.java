package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.sale.ProductSearchResponse;
import com.sergiocodev.app.dto.sale.BarcodeScanResponse;
import com.sergiocodev.app.dto.sale.CartCalculationRequest;
import com.sergiocodev.app.dto.sale.CartCalculationResponse;
import com.sergiocodev.app.dto.ResponseApi;
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
    public ResponseEntity<ResponseApi<SaleResponse>> create(@Valid @RequestBody SaleRequest request,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request, userId), "Venta procesada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todas las ventas")
    public ResponseEntity<ResponseApi<List<SaleResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por ID")
    public ResponseEntity<ResponseApi<SaleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una venta")
    public ResponseEntity<ResponseApi<Void>> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Venta cancelada exitosamente"));
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
    public ResponseEntity<ResponseApi<SaleResponse>> createCreditNote(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ResponseApi.success(service.createCreditNote(id, reason, userId),
                "Nota de crédito emitida exitosamente"));
    }

    @PostMapping("/{id}/debit-note")
    @Operation(summary = "Emitir Nota de Débito")
    public ResponseEntity<ResponseApi<SaleResponse>> createDebitNote(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ResponseApi.success(service.createDebitNote(id, reason, userId),
                "Nota de débito emitida exitosamente"));
    }

    @PostMapping("/{id}/invalidate")
    @Operation(summary = "Invalidar/Baja de documento")
    public ResponseEntity<ResponseApi<Void>> invalidate(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long userId) {
        service.invalidate(id, reason, userId);
        return ResponseEntity.ok(ResponseApi.success(null, "Documento invalidado exitosamente"));
    }

    @GetMapping("/ListProductsForSale")
    @Operation(summary = "Listar produtos para venta con stock y detalles")
    public ResponseEntity<ResponseApi<List<ProductForSaleResponse>>> listProductsForSale(
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.listProductsForSale(establishmentId)));
    }

    @GetMapping("/SearchProductsForPOS")
    @Operation(summary = "Busca productos por nombre o código para POS")
    public ResponseEntity<ResponseApi<List<ProductSearchResponse>>> searchProductsForPOS(
            @RequestParam String query,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.searchProductsForPOS(query, establishmentId)));
    }

    @GetMapping("/GetProductByBarcodeScan")
    @Operation(summary = "Obtener producto por escaneo de código de barras (FEFO)")
    public ResponseEntity<ResponseApi<BarcodeScanResponse>> getProductByBarcodeScan(
            @RequestParam String barcode,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getProductByBarcode(barcode, establishmentId)));
    }

    @PostMapping("/CalculateCartTotals")
    @Operation(summary = "Calcular totales del carrito (impuestos, descuentos)")
    public ResponseEntity<ResponseApi<CartCalculationResponse>> calculateCartTotals(
            @Valid @RequestBody CartCalculationRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.calculateCartTotals(request)));
    }

    @PostMapping("/ProcessSaleTransaction")
    @Operation(summary = "Procesar transacción de venta (POS)")
    public ResponseEntity<ResponseApi<SaleResponse>> processSaleTransaction(
            @Valid @RequestBody SaleRequest request,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.processSaleTransaction(request, userId),
                        "Venta procesada exitosamente"));
    }

    @GetMapping("/GetSaleDocumentPDF")
    @Operation(summary = "Obtener documento de venta (Ticket, A4, 80mm)")
    public ResponseEntity<byte[]> getSaleDocumentPDF(
            @RequestParam Long id,
            @RequestParam(defaultValue = "A4") String format) {
        byte[] pdf = service.getSaleDocumentPDF(id, format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + "-" + format + ".pdf")
                .body(pdf);
    }
}
