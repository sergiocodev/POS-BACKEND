package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.report.DailySalesReport;
import com.sergiocodev.app.dto.report.ProfitabilityReport;
import com.sergiocodev.app.dto.report.SunatStatusReport;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    DailySalesReport getDailySales(LocalDate date, Long establishmentId);

    List<ProfitabilityReport> getProfitability(LocalDate start, LocalDate end, Long establishmentId);

    List<SunatStatusReport> getSunatStatus(Long establishmentId);
}
