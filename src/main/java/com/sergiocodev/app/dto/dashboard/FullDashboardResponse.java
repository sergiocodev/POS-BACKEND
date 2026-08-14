package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FullDashboardResponse(
        DashboardSummaryResponse summary,
        @JsonProperty("cashflow_chart") List<CashflowChartResponse> cashflowChart,
        @JsonProperty("sales_by_category") List<SalesByCategoryResponse> salesByCategory,
        @JsonProperty("top_products") List<TopProductDashboard> topProducts,
        @JsonProperty("employee_performance") List<EmployeePerformanceDashboard> employeePerformance,
        @JsonProperty("recent_transactions") List<RecentTransactionResponse> recentTransactions,
        @JsonProperty("low_stock") List<LowStockItemResponse> lowStock,
        @JsonProperty("expiring_lots") List<ExpiringLotResponse> expiringLots,
        @JsonProperty("sunat_status_distribution") List<SunatStatusDistribution> sunatStatusDistribution,
        @JsonProperty("upcoming_payables") List<AccountPayableDashboardResponse> upcomingPayables) {
}
