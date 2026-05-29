package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchasesByBuyerReport(
        Long buyerId,
        String buyerName,
        int totalPurchases,
        BigDecimal totalSpent,
        LocalDate lastPurchaseDate,
        BigDecimal averagePurchase,
        List<PurchaseDetail> purchases) {

    public record PurchaseDetail(
            LocalDate date,
            String buyerName,
            String supplierName,
            String document,
            BigDecimal total) {
    }
}
