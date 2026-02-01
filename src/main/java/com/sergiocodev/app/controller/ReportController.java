package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.report.DailySalesReport;
import com.sergiocodev.app.dto.report.ProfitabilityReport;
import com.sergiocodev.app.dto.report.SunatStatusReport;
import com.sergiocodev.app.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "API para generación de reportes de negocio")
public class ReportController {

    private final ReportService service;

    @GetMapping("/sales-daily")
    @Operation(summary = "Reporte de ventas del día")
    public ResponseEntity<DailySalesReport> getDailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(service.getDailySales(date, establishmentId));
    }

    @GetMapping("/profitability")
    @Operation(summary = "Reporte de utilidad y rentabilidad")
    public ResponseEntity<List<ProfitabilityReport>> getProfitability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam Long establishmentId) {
        return ResponseEntity.ok(service.getProfitability(start, end, establishmentId));
    }

    @GetMapping("/sunat-status")
    @Operation(summary = "Estado de comprobantes SUNAT")
    public ResponseEntity<List<SunatStatusReport>> getSunatStatus(@RequestParam Long establishmentId) {
        return ResponseEntity.ok(service.getSunatStatus(establishmentId));
    }
}
