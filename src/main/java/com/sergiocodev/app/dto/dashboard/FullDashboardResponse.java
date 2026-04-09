package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FullDashboardResponse(
        DashboardSummaryResponse summary,
        @JsonProperty("sales_chart") List<SalesChartResponse> salesChart,
        @JsonProperty("sales_by_category") List<SalesByCategoryResponse> salesByCategory,
        @JsonProperty("payment_methods") List<PaymentMethodDistribution> paymentMethods,
        @JsonProperty("top_products") List<TopProductDashboard> topProducts,
        @JsonProperty("employee_performance") List<EmployeePerformanceDashboard> employeePerformance,
        @JsonProperty("recent_sales") List<RecentSaleResponse> recentSales,
        @JsonProperty("low_stock") List<LowStockItemResponse> lowStock,
        @JsonProperty("expiring_lots") List<ExpiringLotResponse> expiringLots) {
}
