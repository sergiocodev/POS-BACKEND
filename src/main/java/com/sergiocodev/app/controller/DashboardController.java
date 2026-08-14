package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.dashboard.*;
import com.sergiocodev.app.service.interfaces.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "API para indicadores y gráficos del Dashboard")
@RequiresPermission(PermissionConstants.DASHBOARD)
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

    @GetMapping("/cashflow-chart")
    @Operation(summary = "Gráfico de ingresos vs egresos")
    public ResponseEntity<ResponseApi<List<CashflowChartResponse>>> getCashflowChart(
            @RequestParam(defaultValue = "7days") String range,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getCashflowChart(range, establishmentId)));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Alertas críticas de inventario y SUNAT")
    public ResponseEntity<ResponseApi<DashboardAlertsResponse>> getAlerts(
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getAlerts(establishmentId)));
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

    @GetMapping("/recent-transactions")
    @Operation(summary = "Ventas y compras más recientes del día")
    public ResponseEntity<ResponseApi<List<RecentTransactionResponse>>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getRecentTransactions(limit, establishmentId)));
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
