package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record TopProductDashboard(
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("quantity_sold") BigDecimal quantitySold,
        @JsonProperty("total_amount") BigDecimal totalAmount) {
}
