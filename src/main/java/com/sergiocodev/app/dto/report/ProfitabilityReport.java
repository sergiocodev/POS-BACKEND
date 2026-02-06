package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record ProfitabilityReport(
        Long productId,
        String productName,
        BigDecimal quantitySold,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal grossProfit,
        BigDecimal marginPercentage) {
}
