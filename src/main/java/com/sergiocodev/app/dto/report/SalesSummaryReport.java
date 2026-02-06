package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record SalesSummaryReport(
        LocalDate startDate,
        LocalDate endDate,
        Long totalTransactions,
        BigDecimal totalRevenue,
        BigDecimal totalTax,
        Long voidedCount,
        BigDecimal voidedAmount,
        Map<String, Long> countByDocumentType,
        Map<String, BigDecimal> amountByDocumentType) {
}
