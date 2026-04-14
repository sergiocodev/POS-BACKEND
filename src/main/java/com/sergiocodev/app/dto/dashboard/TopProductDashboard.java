package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record TopProductDashboard(
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("quantity_sold") BigDecimal quantitySold,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        @JsonProperty("trend_label") String trendLabel) {
}
