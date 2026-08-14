package com.sergiocodev.app.controller;

import com.sergiocodev.app.config.UserPrincipal;
import com.sergiocodev.app.dto.sale.ProductSearchResponse;
import com.sergiocodev.app.dto.sale.BarcodeScanResponse;
import com.sergiocodev.app.dto.sale.CartCalculationRequest;
import com.sergiocodev.app.dto.sale.CartCalculationResponse;
import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import com.sergiocodev.app.service.interfaces.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import com.sergiocodev.app.config.ApiVersion;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Endpoints para la gestión de ventas")
@SecurityRequirement(name = "bearerAuth")
@ApiVersion(1)
public class SaleController {

    private final SaleService service;

    @PostMapping
    @Operation(summary = "Procesar una nueva venta")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<SaleResponse>> create(@Valid @RequestBody SaleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request, principal.getId()), "Venta procesada exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar todas las ventas")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<ResponseApi<List<SaleResponse>>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(ResponseApi.success(service.getAll(startDate, endDate)));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar ventas con paginación")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<ResponseApi<Page<SaleResponse>>> getAllPaged(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerDocument,
            @RequestParam(required = false) String vendedorName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sunatStatus,
            @RequestParam(required = false) String total,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String columnDate,
            @RequestParam(required = false) Long establishmentId,
            @PageableDefault(size = 50, sort = "date", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity
                .ok(ResponseApi.success(service.getAllPaged(startDate, endDate, documentType, series, number,
                        customerName, customerDocument, vendedorName, status, sunatStatus, total, paymentMethod,
                        columnDate, establishmentId, pageable)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener resumen de totales filtrados")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<ResponseApi<com.sergiocodev.app.dto.sale.SaleSummaryResponse>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerDocument,
            @RequestParam(required = false) String vendedorName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sunatStatus,
            @RequestParam(required = false) String total,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String columnDate,
            @RequestParam(required = false) Long establishmentId) {

        return ResponseEntity.ok(ResponseApi.success(service.getSummary(startDate, endDate, documentType, series,
                number,
                customerName, customerDocument, vendedorName, status, sunatStatus, total, paymentMethod, columnDate,
                establishmentId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por ID")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<ResponseApi<SaleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una venta")
    @RequiresPermission(PermissionConstants.ROL_ADMINISTRADOR)
    public ResponseEntity<ResponseApi<Void>> cancel(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        service.cancel(id, principal.getId());
        return ResponseEntity.ok(ResponseApi.success(null, "Venta cancelada exitosamente"));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descargar PDF/Ticket")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        byte[] pdf = service.getPdf(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/{id}/xml")
    @Operation(summary = "Descargar XML")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<String> getXml(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + ".xml")
                .body(service.getXml(id));
    }

    @GetMapping("/{id}/cdr")
    @Operation(summary = "Descargar CDR (Constancia SUNAT)")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<String> getCdr(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + "-cdr.xml")
                .body(service.getCdr(id));
    }

    @PostMapping("/{id}/credit-note")
    @Operation(summary = "Emitir Nota de Crédito")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<SaleResponse>> createCreditNote(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ResponseApi.success(service.createCreditNote(id, reason, principal.getId()),
                "Nota de crédito emitida exitosamente"));
    }

    @PostMapping("/{id}/debit-note")
    @Operation(summary = "Emitir Nota de Débito")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<SaleResponse>> createDebitNote(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ResponseApi.success(service.createDebitNote(id, reason, principal.getId()),
                "Nota de débito emitida exitosamente"));
    }

    @PostMapping("/{id}/invalidate")
    @Operation(summary = "Invalidar/Baja de documento")
    @RequiresPermission(PermissionConstants.ROL_ADMINISTRADOR)
    public ResponseEntity<ResponseApi<Void>> invalidate(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        service.invalidate(id, reason, principal.getId());
        return ResponseEntity.ok(ResponseApi.success(null, "Documento invalidado exitosamente"));
    }

    @GetMapping("/ListProductsForSale")
    @Operation(summary = "Listar produtos para venta con stock y detalles")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<List<ProductForSaleResponse>>> listProductsForSale(
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.listProductsForSale(establishmentId)));
    }

    @GetMapping("/SearchProductsForPOS")
    @Operation(summary = "Busca productos por nombre o código para POS")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<List<ProductSearchResponse>>> searchProductsForPOS(
            @RequestParam String query,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.searchProductsForPOS(query, establishmentId)));
    }

    @GetMapping("/GetProductByBarcodeScan")
    @Operation(summary = "Obtener producto por escaneo de código de barras (FEFO)")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<BarcodeScanResponse>> getProductByBarcodeScan(
            @RequestParam String barcode,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getProductByBarcode(barcode, establishmentId)));
    }

    @PostMapping("/CalculateCartTotals")
    @Operation(summary = "Calcular totales del carrito (impuestos, descuentos)")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<CartCalculationResponse>> calculateCartTotals(
            @Valid @RequestBody CartCalculationRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.calculateCartTotals(request)));
    }

    @PostMapping("/ProcessSaleTransaction")
    @Operation(summary = "Procesar transacción de venta (POS)")
    @RequiresPermission(PermissionConstants.VENTAS_POS)
    public ResponseEntity<ResponseApi<SaleResponse>> processSaleTransaction(
            @Valid @RequestBody SaleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.processSaleTransaction(request, principal.getId()),
                        "Venta procesada exitosamente"));
    }

    @GetMapping("/GetSaleDocumentPDF")
    @Operation(summary = "Obtener documento de venta (Ticket, A4, 80mm)")
    @RequiresPermission(PermissionConstants.VENTAS_LISTA)
    public ResponseEntity<byte[]> getSaleDocumentPDF(
            @RequestParam Long id,
            @RequestParam(defaultValue = "A4") String format) {
        byte[] pdf = service.getSaleDocumentPDF(id, format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sale-" + id + "-" + format + ".pdf")
                .body(pdf);
    }
}
