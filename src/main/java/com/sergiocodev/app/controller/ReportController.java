package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.dto.report.SalesByCustomerReport;
import com.sergiocodev.app.service.interfaces.ReportService;
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
import com.sergiocodev.app.service.interfaces.CompanyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getDailySales(date, establishmentId)));
    }

    @GetMapping("/profitability")
    @Operation(summary = "Reporte de utilidad y rentabilidad")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<ProfitabilityReport>>> getProfitability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByCategory(start, end, establishmentId)));
    }

    @GetMapping("/sales-by-employee")
    @Operation(summary = "Rendimiento de Vendedores")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<EmployeeSalesReport>>> getSalesByEmployee(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByEmployee(start, end, establishmentId)));
    }

    @GetMapping("/hourly-heat")
    @Operation(summary = "Mapa de Calor por Horas")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<HourlyHeatReport>>> getHourlyHeat(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getPurchases(start, end, establishmentId)));
    }

    @GetMapping("/sales")
    @Operation(summary = "Reporte de ventas detallado por período")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesReport>>> getSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSales(start, end, establishmentId)));
    }

    @GetMapping("/sales/summary")
    @Operation(summary = "Resumen agregado de ventas por período")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<SalesSummaryReport>> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesSummary(start, end, establishmentId)));
    }

    // ── Nuevos endpoints para reportes de farmacia ──

    @GetMapping("/sales/filtered")
    @Operation(summary = "Ventas filtradas por tipo de comprobante y serie")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesReport>>> getSalesFiltered(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) com.sergiocodev.app.model.Sale.SaleDocumentType documentType,
            @RequestParam(required = false) String series) {
        return ResponseEntity.ok(ResponseApi.success(
                service.getSalesFiltered(start, end, establishmentId, documentType, series, null)));
    }

    @GetMapping("/pdf/comprobantes")
    @Operation(summary = "Generar PDF de reporte por comprobantes")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesFilteredPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) com.sergiocodev.app.model.Sale.SaleDocumentType documentType,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) Long sellerId) throws Exception {
            
        List<SalesReport> sales = service.getSalesFiltered(start, end, establishmentId, documentType, series, sellerId);
        byte[] pdfBytes = ReportPdfGenerator.generateComprobantesReport(
                sales, 
                companyService.getCompany(), 
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")), 
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"))
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesBySeries(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-payment-method")
    @Operation(summary = "Ventas agrupadas por método de pago")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByPaymentMethodReport>>> getSalesByPaymentMethod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByPaymentMethod(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-laboratory")
    @Operation(summary = "Ventas agrupadas por laboratorio")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByLaboratoryReport>>> getSalesByLaboratory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByLaboratory(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-employee-category")
    @Operation(summary = "Ventas cruzadas por vendedor y categoría")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByEmployeeCategoryReport>>> getSalesByEmployeeCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getSalesByEmployeeCategory(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-category-detail")
    @Operation(summary = "Ventas por categoría con detalle de productos")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByCategoryDetailReport>>> getSalesByCategoryDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getSalesByCategoryDetail(start, end, establishmentId)));
    }

    @GetMapping("/sales/by-product")
    @Operation(summary = "Ventas agrupadas por producto")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<com.sergiocodev.app.dto.report.SalesByProductReport>>> getSalesByProduct(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getSalesByProduct(start, end, establishmentId)));
    }

    @GetMapping("/sales/series")
    @Operation(summary = "Obtener series disponibles por establecimiento y tipo de documento")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<String>>> getAvailableSeries(
            @RequestParam Long establishmentId,
            @RequestParam(required = false) com.sergiocodev.app.model.Sale.SaleDocumentType documentType) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getAvailableSeries(establishmentId, documentType)));
    }

    @GetMapping("/pdf/categories")
    @Operation(summary = "Generar PDF de reporte por categorías")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesByCategoryPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Long sellerId) throws Exception {

        List<SalesByCategoryDetailReport> reports = service.getSalesByCategories(start, end, establishmentId,
                categoryIds, sellerId);
        byte[] pdfBytes = ReportPdfGenerator.generateCategoriesReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_categorias.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/pdf/products")
    @Operation(summary = "Generar PDF de reporte por productos con filtros")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesByProductFiltersPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> productIds,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(required = false) List<Long> therapeuticActionIds,
            @RequestParam(required = false) Long sellerId) throws Exception {

        List<SalesByProductReport> reports = service.getSalesByProductFilters(start, end, establishmentId,
                productIds, brandIds, therapeuticActionIds, sellerId);
        byte[] pdfBytes = ReportPdfGenerator.generateProductsReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_productos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/pdf/series")
    @Operation(summary = "Generar PDF de reporte por establecimiento y serie")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesBySeriesPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<String> series) throws Exception {

        List<SalesBySeriesReport> reports = service.getSalesBySeriesFiltered(start, end, establishmentId, series);
        byte[] pdfBytes = ReportPdfGenerator.generateSeriesReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_series.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // ── Seller-specific PDF endpoints ──

    @GetMapping("/pdf/seller")
    @Operation(summary = "Generar PDF de ventas por vendedor")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesBySellerPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> sellerIds) throws Exception {

        List<SalesReport> reports = service.getSalesBySeller(start, end, establishmentId, sellerIds);
        String sellerName = service.getSellerNames(sellerIds);

        byte[] pdfBytes = ReportPdfGenerator.generateSellerReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                sellerName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_vendedor.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/pdf/seller-categories")
    @Operation(summary = "Generar PDF de ventas por categoría de un vendedor")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesBySellerCategoriesPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> sellerIds,
            @RequestParam(required = false) List<Long> categoryIds) throws Exception {

        List<SalesByCategoryDetailReport> reports = service.getSalesBySellerCategories(start, end,
                establishmentId, sellerIds, categoryIds);
        String sellerName = service.getSellerNames(sellerIds);

        byte[] pdfBytes = ReportPdfGenerator.generateSellerCategoriesReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                sellerName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_vendedor_categorias.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/pdf/seller-products")
    @Operation(summary = "Generar PDF de ventas por producto de un vendedor")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesBySellerProductsPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> sellerIds,
            @RequestParam(required = false) List<Long> productIds) throws Exception {

        List<SalesByProductReport> reports = service.getSalesBySellerProducts(start, end,
                establishmentId, sellerIds, productIds);
        String sellerName = service.getSellerNames(sellerIds);

        byte[] pdfBytes = ReportPdfGenerator.generateSellerProductsReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                sellerName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_vendedor_productos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // ── Customer-specific endpoints ──

    @GetMapping("/sales/by-customer")
    @Operation(summary = "Ventas agrupadas por cliente")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<ResponseApi<List<SalesByCustomerReport>>> getSalesByCustomer(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> customerIds) {
        return ResponseEntity.ok(
                ResponseApi.success(service.getSalesByCustomer(start, end, establishmentId, customerIds)));
    }

    @GetMapping("/pdf/customer")
    @Operation(summary = "Generar PDF de ventas por cliente")
    @PreAuthorize("hasAuthority('" + PermissionConstants.VENTAS_REPORTES + "')")
    public ResponseEntity<byte[]> getSalesByCustomerPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> customerIds) throws Exception {

        List<SalesByCustomerReport> reports = service.getSalesByCustomer(start, end, establishmentId, customerIds);
        List<SalesReport> details = service.getSalesByCustomerDetail(start, end, establishmentId, customerIds);

        byte[] pdfBytes = ReportPdfGenerator.generateCustomerReport(
                reports,
                details,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_cliente.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // ── Purchase PDF Endpoints ──

    @GetMapping("/pdf/purchases/comprobantes")
    @Operation(summary = "Generar PDF de compras")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<byte[]> getPurchasesFilteredPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) throws Exception {

        List<PurchaseReport> purchases = service.getPurchases(start, end, establishmentId);
        byte[] pdfBytes = ReportPdfGenerator.generatePurchaseComprobantesReport(
                purchases,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_compras_comprobantes.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/purchases/status")
    @Operation(summary = "Generar PDF de compras por estado")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<byte[]> getPurchasesByStatusPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) throws Exception {

        List<com.sergiocodev.app.dto.report.PurchaseDebtReport> debts = service.getPurchaseDebtStatus(start, end, establishmentId);
        
        byte[] pdfBytes = com.sergiocodev.app.util.PurchaseDebtPdfGenerator.generateDebtReport(
                debts,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_compras_estado.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/purchases/categories")
    @Operation(summary = "Generar PDF de compras por categoria")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<byte[]> getPurchasesByCategoryPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> categoryIds) throws Exception {

        List<com.sergiocodev.app.dto.report.PurchasesByCategoryDetailReport> purchases = service.getPurchasesByCategoryDetail(start, end, establishmentId, categoryIds);
        byte[] pdfBytes = ReportPdfGenerator.generatePurchaseCategoriesReport(
                purchases,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_compras_categorias.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/purchases/price-history")
    @Operation(summary = "Generar PDF de historial de precios")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<byte[]> getProductPriceHistoryPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) Long productId) throws Exception {

        List<com.sergiocodev.app.dto.report.ProductPriceHistoryReport> reports = service.getProductPriceHistory(start, end, establishmentId, productId);
        byte[] pdfBytes = ReportPdfGenerator.generateProductPriceHistoryReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_precios_compra.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/purchases/supplier")
    @Operation(summary = "Generar PDF de compras por proveedor")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<byte[]> getPurchasesBySupplierPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> supplierIds) throws Exception {

        List<com.sergiocodev.app.dto.report.PurchasesBySupplierReport> purchases = service.getPurchasesBySupplier(start, end, establishmentId, supplierIds);
        
        byte[] pdfBytes = ReportPdfGenerator.generatePurchasesBySupplierReport(
                purchases,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_compras_proveedor.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/purchases/accounts-payable")
    @Operation(summary = "Generar PDF de cuentas por pagar")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<byte[]> getAccountsPayableBySupplierPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> supplierIds) throws Exception {

        List<com.sergiocodev.app.dto.report.AccountsPayableSupplierReport> reports = service.getAccountsPayableBySupplier(start, end, establishmentId, supplierIds);
        
        byte[] pdfBytes = ReportPdfGenerator.generateAccountsPayableBySupplierReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_cuentas_pagar.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/purchases/buyer")
    @Operation(summary = "Generar PDF de compras por comprador")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COMPRAS_REPORTES + "')")
    public ResponseEntity<byte[]> getPurchasesByBuyerPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId,
            @RequestParam(required = false) List<Long> buyerIds) throws Exception {

        List<com.sergiocodev.app.dto.report.PurchasesByBuyerReport> reports = service.getPurchasesByBuyer(start, end, establishmentId, buyerIds);
        byte[] pdfBytes = ReportPdfGenerator.generatePurchasesByBuyerReport(
                reports,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_compras_comprador.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ── Cash Box PDF Endpoints ──

    @GetMapping("/pdf/cash/sessions")
    @Operation(summary = "Generar PDF de historial de sesiones de caja")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CAJA_REPORTES + "')")
    public ResponseEntity<byte[]> getCashSessionsPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) throws Exception {

        List<com.sergiocodev.app.dto.report.CashSessionReport> sessions = service.getCashSessions(start, end, establishmentId);
        byte[] pdfBytes = ReportPdfGenerator.generateCashSessionsReport(
                sessions,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_sesiones_caja.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/cash/movements")
    @Operation(summary = "Generar PDF de movimientos de caja por período")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CAJA_REPORTES + "')")
    public ResponseEntity<byte[]> getCashMovementsPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long establishmentId) throws Exception {

        List<com.sergiocodev.app.dto.report.CashMovementReport> movements = service.getCashMovementsByPeriod(start, end, establishmentId);
        byte[] pdfBytes = ReportPdfGenerator.generateCashMovementsReport(
                movements,
                companyService.getCompany(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_movimientos_caja.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/pdf/cash/sessions/{sessionId}/arqueo")
    @Operation(summary = "Generar PDF de arqueo de una sesión de caja")
    @PreAuthorize("hasAuthority('" + PermissionConstants.CAJA_REPORTES + "')")
    public ResponseEntity<byte[]> getCashArqueoPdf(
            @PathVariable Long sessionId) throws Exception {

        List<com.sergiocodev.app.dto.report.CashSessionReport> sessions = service.getCashSessions(
                java.time.LocalDateTime.of(2000, 1, 1, 0, 0),
                java.time.LocalDateTime.now(),
                null);

        com.sergiocodev.app.dto.report.CashSessionReport session = sessions.stream()
                .filter(s -> s.sessionId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new com.sergiocodev.app.exception.ResourceNotFoundException("Session not found: " + sessionId));

        List<com.sergiocodev.app.dto.report.CashMovementReport> movements = service.getCashMovementsBySession(sessionId);

        byte[] pdfBytes = ReportPdfGenerator.generateCashArqueoReport(
                session,
                movements,
                companyService.getCompany());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "arqueo_caja_" + sessionId + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}

