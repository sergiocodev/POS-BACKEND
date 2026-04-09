package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentSaleResponse(
        @JsonProperty("sale_id") Long saleId,
        @JsonProperty("customer_name") String customerName,
        @JsonProperty("customer_initials") String customerInitials,
        @JsonProperty("document_type") String documentType,
        @JsonProperty("product_count") int productCount,
        @JsonProperty("sale_date") LocalDateTime saleDate,
        BigDecimal total) {
}
