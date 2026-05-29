package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record PurchasesByCategoryDetailReport(
        Long categoryId,
        String categoryName,
        BigDecimal totalSpent,
        BigDecimal quantityPurchased,
        Long productCount,
        List<ProductDetail> products) {

    public record ProductDetail(
            Long productId,
            String productName,
            String laboratoryName,
            BigDecimal quantityPurchased,
            BigDecimal spent) {
    }
}
