package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesReport(
        LocalDate date,
        Long transactionCount,
        BigDecimal totalSales,
        BigDecimal totalTax,
        BigDecimal totalDiscount) {
}
