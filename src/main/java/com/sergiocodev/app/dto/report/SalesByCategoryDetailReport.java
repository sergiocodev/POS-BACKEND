package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record SalesByCategoryDetailReport(
        Long categoryId,
        String categoryName,
        BigDecimal totalRevenue,
        BigDecimal quantitySold,
        Long productCount,
        List<ProductDetail> products) {

    public record ProductDetail(
            Long productId,
            String productName,
            String laboratoryName,
            BigDecimal quantitySold,
            BigDecimal revenue) {
    }
}
