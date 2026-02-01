package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private String period;
    private SummaryData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryData {
        @JsonProperty("total_sales")
        private ValueTrend totalSales;
        private ValueTrendLong transactions;
        @JsonProperty("sunat_pending_docs")
        private long sunatPendingDocs;
        @JsonProperty("stock_alerts")
        private StockAlertsData stockAlerts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueTrend {
        private BigDecimal value;
        private String currency;
        private String trend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueTrendLong {
        private long value;
        private String trend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockAlertsData {
        private long expired;
        @JsonProperty("expiring_soon")
        private long expiringSoon;
        @JsonProperty("out_of_stock")
        private long outOfStock;
    }
}
