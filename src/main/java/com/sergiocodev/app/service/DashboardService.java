package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummaryCards(Long establishmentId);

    List<SalesChartResponse> getSalesChart(String range, Long establishmentId);

    DashboardAlertsResponse getAlerts(Long establishmentId);

    List<PaymentMethodDistribution> getPaymentMethods(LocalDate date, Long establishmentId);

    List<TopProductDashboard> getTopProducts(int limit, Long establishmentId);

    List<EmployeePerformanceDashboard> getEmployeePerformance(LocalDate date, Long establishmentId);
}
