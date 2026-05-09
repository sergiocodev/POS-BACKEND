package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record SalesByCustomerReport(
        Long customerId,
        String customerName,
        String documentNumber,
        long transactionCount,
        BigDecimal totalRevenue) {
}
