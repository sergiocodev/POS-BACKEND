package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record SalesByEmployeeCategoryReport(
        Long userId,
        String userName,
        String categoryName,
        BigDecimal totalRevenue,
        BigDecimal quantitySold,
        Long transactionCount) {
}
