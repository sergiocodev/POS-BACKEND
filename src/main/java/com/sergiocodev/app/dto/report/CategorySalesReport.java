package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record CategorySalesReport(
        Long categoryId,
        String categoryName,
        BigDecimal totalRevenue,
        BigDecimal quantitySold) {
}
