package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record DashboardSummaryResponse(
        String period,
        SummaryData data) {
    public record SummaryData(
            @JsonProperty("total_sales") ValueTrend totalSales,
            ValueTrendLong transactions,
            @JsonProperty("sunat_pending_docs") long sunatPendingDocs,
            @JsonProperty("stock_alerts") StockAlertsData stockAlerts) {
    }

    public record ValueTrend(
            BigDecimal value,
            String currency,
            String trend) {
    }

    public record ValueTrendLong(
            long value,
            String trend) {
    }

    public record StockAlertsData(
            long expired,
            @JsonProperty("expiring_soon") long expiringSoon,
            @JsonProperty("out_of_stock") long outOfStock) {
    }
}
