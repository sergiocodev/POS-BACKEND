package com.sergiocodev.app.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LowStockItemResponse(
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("current_stock") int currentStock,
        @JsonProperty("min_stock") int minStock,
        @JsonProperty("stock_level") double stockLevel,
        boolean critical) {
}
