package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import com.sergiocodev.app.util.ReportPdfGenerator;
import com.sergiocodev.app.service.CompanyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "API para generación de reportes de negocio")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService service;
    private final CompanyService companyService;

    public ReportController(ReportService service, CompanyService companyService) {
        this.service = service;
        this.companyService = companyService;
    }

    @GetMapping("/sales-daily")
    @Operation(summary = "Reporte de ventas del día")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<DailySalesReport>> getDailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getDailySales(date, establishmentId)));
    }

    @GetMapping("/profitability")
    @Operation(summary = "Reporte de utilidad y rentabilidad")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<ProfitabilityReport>>> getProfitability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getProfitability(start, end, establishmentId)));
    }

    @GetMapping("/sunat-status")
    @Operation(summary = "Estado de comprobantes SUNAT")
    @PreAuthorize("hasAuthority('" + PermissionConstants.FACTURACION_COMPROBANTES + "')")
    public ResponseEntity<ResponseApi<List<SunatStatusReport>>> getSunatStatus(@RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSunatStatus(establishmentId)));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Ranking de Productos (Pareto)")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<TopProductReport>>> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId,
            @RequestParam(defaultValue = "amount") String sortBy,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity
                .ok(ResponseApi.success(service.getTopProducts(start, end, establishmentId, sortBy, limit)));
    }

    @GetMapping("/sales-by-category")
    @Operation(summary = "Ventas por Familia/Categoría")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<CategorySalesReport>>> getSalesByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByCategory(start, end, establishmentId)));
    }

    @GetMapping("/sales-by-employee")
    @Operation(summary = "Rendimiento de Vendedores")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<EmployeeSalesReport>>> getSalesByEmployee(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByEmployee(start, end, establishmentId)));
    }

    @GetMapping("/hourly-heat")
    @Operation(summary = "Mapa de Calor por Horas")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<HourlyHeatReport>>> getHourlyHeat(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getHourlyHeat(start, end, establishmentId)));
    }

    @GetMapping("/low-rotation")
    @Operation(summary = "Productos de Baja Rotación (Huesos)")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<LowRotationReport>>> getLowRotation(
            @RequestParam(defaultValue = "90") int days,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getLowRotation(days, establishmentId)));
    }

    @GetMapping("/purchases")
    @Operation(summary = "Reporte de compras por período")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<PurchaseReport>>> getPurchases(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getPurchases(start, end, establishmentId)));
    }

    @GetMapping("/sales")
    @Operation(summary = "Reporte de ventas detallado por período")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesReport>>> getSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSales(start, end, establishmentId)));
    }

    @GetMapping("/sales/summary")
    @Operation(summary = "Resumen agregado de ventas por período")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<SalesSummaryReport>> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesSummary(start, end, establishmentId)));
    }

    // ── Nuevos endpoints para reportes de farmacia ──

    @GetMapping("/sales/filtered")
    @Operation(summary = "Ventas filtradas por tipo de comprobante y serie")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesReport>>> getSalesFiltered(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) com.sergiocodev.app.model.Sale.SaleDocumentType documentType,
            @RequestParam(required = false) String series) {
        return ResponseEntity.ok(ResponseApi.success(
                service.getSalesFiltered(start, end, establishmentId, documentType, series)));
    }

    @GetMapping("/pdf/comprobantes")
    @Operation(summary = "Generar PDF de reporte por comprobantes")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesFilteredPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) com.sergiocodev.app.model.Sale.SaleDocumentType documentType,
            @RequestParam(required = false) String series) throws Exception {
            
        List<SalesReport> sales = service.getSalesFiltered(start, end, establishmentId, documentType, series);
        byte[] pdfBytes = ReportPdfGenerator.generateComprobantesReport(
                sales, 
                companyService.getCompany(), 
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_comprobantes.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/sales/by-series")
    @Operation(summary = "Ventas agrupadas por serie de comprobante")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesBySeriesReport>>> getSalesBySeries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesBySeries(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-payment-method")
    @Operation(summary = "Ventas agrupadas por método de pago")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByPaymentMethodReport>>> getSalesByPaymentMethod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByPaymentMethod(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-laboratory")
    @Operation(summary = "Ventas agrupadas por laboratorio")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByLaboratoryReport>>> getSalesByLaboratory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByLaboratory(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-employee-category")
    @Operation(summary = "Ventas cruzadas por vendedor y categoría")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByEmployeeCategoryReport>>> getSalesByEmployeeCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getSalesByEmployeeCategory(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-category-detail")
    @Operation(summary = "Ventas por categoría con detalle de productos")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByCategoryDetailReport>>> getSalesByCategoryDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getSalesByCategoryDetail(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-product")
    @Operation(summary = "Ventas agrupadas por producto")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<com.sergiocodev.app.dto.report.SalesByProductReport>>> getSalesByProduct(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getSalesByProduct(start, end, establishmentId)));
    }

    @GetMapping("/sales/series")
    @Operation(summary = "Obtener series disponibles por establecimiento y tipo de documento")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<String>>> getAvailableSeries(
            @RequestParam Long establishmentId,
            @RequestParam com.sergiocodev.app.model.Sale.SaleDocumentType documentType) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getAvailableSeries(establishmentId, documentType)));
    }
}

