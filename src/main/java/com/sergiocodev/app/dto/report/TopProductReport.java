package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record TopProductReport(
        Long productId,
        String productName,
        BigDecimal value, // Quantity or Amount
        BigDecimal percentage,
        BigDecimal cumulativePercentage) {
    // Note: If you need to set cumulativePercentage later,
    // you might need a custom constructor or a with method since records are
    // immutable.
    // However, for typical DTO usage, this should be fine.
}
