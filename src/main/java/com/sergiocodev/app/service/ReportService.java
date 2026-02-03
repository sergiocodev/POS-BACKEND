package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.report.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    DailySalesReport getDailySales(LocalDate date, Long establishmentId);

    List<ProfitabilityReport> getProfitability(LocalDate start, LocalDate end, Long establishmentId);

    List<SunatStatusReport> getSunatStatus(Long establishmentId);

    List<TopProductReport> getTopProducts(LocalDate start, LocalDate end, Long establishmentId, String sortBy,
            int limit);

    List<CategorySalesReport> getSalesByCategory(LocalDate start, LocalDate end, Long establishmentId);

    List<EmployeeSalesReport> getSalesByEmployee(LocalDate start, LocalDate end, Long establishmentId);

    List<HourlyHeatReport> getHourlyHeat(LocalDate start, LocalDate end, Long establishmentId);

    List<LowRotationReport> getLowRotation(int days, Long establishmentId);

    List<PurchaseReport> getPurchases(LocalDate start, LocalDate end, Long establishmentId);

    List<SalesReport> getSales(LocalDate start, LocalDate end, Long establishmentId);

    SalesSummaryReport getSalesSummary(LocalDate start, LocalDate end, Long establishmentId);
}
