package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "API para generación de reportes de negocio")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/sales-daily")
    @Operation(summary = "Reporte de ventas del día")
    public ResponseEntity<ResponseApi<DailySalesReport>> getDailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getDailySales(date, establishmentId)));
    }

    @GetMapping("/profitability")
    @Operation(summary = "Reporte de utilidad y rentabilidad")
    public ResponseEntity<ResponseApi<List<ProfitabilityReport>>> getProfitability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getProfitability(start, end, establishmentId)));
    }

    @GetMapping("/sunat-status")
    @Operation(summary = "Estado de comprobantes SUNAT")
    public ResponseEntity<ResponseApi<List<SunatStatusReport>>> getSunatStatus(@RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSunatStatus(establishmentId)));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Ranking de Productos (Pareto)")
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
    public ResponseEntity<ResponseApi<List<CategorySalesReport>>> getSalesByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByCategory(start, end, establishmentId)));
    }

    @GetMapping("/sales-by-employee")
    @Operation(summary = "Rendimiento de Vendedores")
    public ResponseEntity<ResponseApi<List<EmployeeSalesReport>>> getSalesByEmployee(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesByEmployee(start, end, establishmentId)));
    }

    @GetMapping("/hourly-heat")
    @Operation(summary = "Mapa de Calor por Horas")
    public ResponseEntity<ResponseApi<List<HourlyHeatReport>>> getHourlyHeat(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getHourlyHeat(start, end, establishmentId)));
    }

    @GetMapping("/low-rotation")
    @Operation(summary = "Productos de Baja Rotación (Huesos)")
    public ResponseEntity<ResponseApi<List<LowRotationReport>>> getLowRotation(
            @RequestParam(defaultValue = "90") int days,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getLowRotation(days, establishmentId)));
    }

    @GetMapping("/purchases")
    @Operation(summary = "Reporte de compras por período")
    public ResponseEntity<ResponseApi<List<PurchaseReport>>> getPurchases(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getPurchases(start, end, establishmentId)));
    }

    @GetMapping("/sales")
    @Operation(summary = "Reporte de ventas detallado por período")
    public ResponseEntity<ResponseApi<List<SalesReport>>> getSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSales(start, end, establishmentId)));
    }

    @GetMapping("/sales/summary")
    @Operation(summary = "Resumen agregado de ventas por período")
    public ResponseEntity<ResponseApi<SalesSummaryReport>> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(ResponseApi.success(service.getSalesSummary(start, end, establishmentId)));
    }
}
