package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record SalesBySeriesReport(
        String documentType,
        String series,
        Long initialNumber,
        Long currentNumber,
        Long transactionCount,
        BigDecimal totalSubTotal,
        BigDecimal totalTax,
        BigDecimal totalAmount,
        Long voidedCount,
        BigDecimal voidedAmount) {
}
