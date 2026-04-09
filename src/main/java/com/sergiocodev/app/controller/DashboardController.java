package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.dashboard.*;
import com.sergiocodev.app.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sergiocodev.app.util.PermissionConstants;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "API para indicadores y gráficos del Dashboard")
@PreAuthorize("hasAuthority('" + PermissionConstants.DASHBOARD + "')")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary-cards")
    @Operation(summary = "Tarjetas de resumen KPIs")
    public ResponseEntity<ResponseApi<DashboardSummaryResponse>> getSummaryCards(
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSummaryCards(establishmentId)));
    }

    @GetMapping("/sales-chart")
    @Operation(summary = "Gráfico de tendencia de ventas")
    public ResponseEntity<ResponseApi<List<SalesChartResponse>>> getSalesChart(
            @RequestParam(defaultValue = "7days") String range,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesChart(range, establishmentId)));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Alertas críticas de inventario y SUNAT")
    public ResponseEntity<ResponseApi<DashboardAlertsResponse>> getAlerts(
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getAlerts(establishmentId)));
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Distribución de métodos de pago")
    public ResponseEntity<ResponseApi<List<PaymentMethodDistribution>>> getPaymentMethods(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long establishmentId) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ResponseApi.success(service.getPaymentMethods(searchDate, establishmentId)));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Top productos más vendidos")
    public ResponseEntity<ResponseApi<List<TopProductDashboard>>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getTopProducts(limit, establishmentId)));
    }

    @GetMapping("/employee-performance")
    @Operation(summary = "Rendimiento de colaboradores")
    public ResponseEntity<ResponseApi<List<EmployeePerformanceDashboard>>> getEmployeePerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long establishmentId) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ResponseApi.success(service.getEmployeePerformance(searchDate, establishmentId)));
    }

    // ── Nuevos endpoints ──────────────────────────────

    @GetMapping("/sales-by-category")
    @Operation(summary = "Distribución de ventas por categoría de producto")
    public ResponseEntity<ResponseApi<List<SalesByCategoryResponse>>> getSalesByCategory(
            @RequestParam(defaultValue = "7days") String range,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByCategory(range, establishmentId)));
    }

    @GetMapping("/recent-sales")
    @Operation(summary = "Ventas más recientes del día")
    public ResponseEntity<ResponseApi<List<RecentSaleResponse>>> getRecentSales(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getRecentSales(limit, establishmentId)));
    }

    @GetMapping("/expiring-lots")
    @Operation(summary = "Lotes próximos a vencer")
    public ResponseEntity<ResponseApi<List<ExpiringLotResponse>>> getExpiringLots(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getExpiringLots(days, establishmentId)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Productos con stock bajo detallado")
    public ResponseEntity<ResponseApi<List<LowStockItemResponse>>> getLowStock(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getLowStockItems(limit, establishmentId)));
    }

    @GetMapping("/full")
    @Operation(summary = "Dashboard completo unificado (todos los datos en una sola llamada)")
    public ResponseEntity<ResponseApi<FullDashboardResponse>> getFullDashboard(
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getFullDashboard(establishmentId)));
    }
}
