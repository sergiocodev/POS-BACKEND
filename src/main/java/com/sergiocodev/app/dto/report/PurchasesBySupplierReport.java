package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchasesBySupplierReport(
        Long supplierId,
        String supplierName,
        long purchaseCount,
        BigDecimal totalSpent,
        LocalDate lastPurchaseDate,
        String status,
        java.util.List<ProductDetail> products) {

    public record ProductDetail(
            String productName,
            String laboratoryName,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal total) {
    }
}
