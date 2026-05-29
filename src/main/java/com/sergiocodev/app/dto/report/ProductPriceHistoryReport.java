package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductPriceHistoryReport(
        Long productId,
        String productName,
        BigDecimal currentPrice,
        BigDecimal lowestPrice,
        BigDecimal highestPrice,
        String totalVariation,
        List<PriceHistoryDetail> history) {

    public record PriceHistoryDetail(
            LocalDate date,
            String productName,
            String supplierName,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String variation) {
    }
}
