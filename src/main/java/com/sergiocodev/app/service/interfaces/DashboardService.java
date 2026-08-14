package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummaryCards(Long establishmentId);

    List<CashflowChartResponse> getCashflowChart(String range, Long establishmentId);

    DashboardAlertsResponse getAlerts(Long establishmentId);

    List<TopProductDashboard> getTopProducts(int limit, Long establishmentId);

    List<EmployeePerformanceDashboard> getEmployeePerformance(LocalDate date, Long establishmentId);

    List<SalesByCategoryResponse> getSalesByCategory(String range, Long establishmentId);

    List<RecentTransactionResponse> getRecentTransactions(int limit, Long establishmentId);

    List<ExpiringLotResponse> getExpiringLots(int days, Long establishmentId);

    List<LowStockItemResponse> getLowStockItems(int limit, Long establishmentId);

    List<SunatStatusDistribution> getSunatStatusDistribution(String range, Long establishmentId);

    List<AccountPayableDashboardResponse> getUpcomingPayables(int limit, Long establishmentId);

    FullDashboardResponse getFullDashboard(Long establishmentId);
}
