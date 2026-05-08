package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record SalesByPaymentMethodReport(
        String paymentMethod,
        Long transactionCount,
        BigDecimal totalAmount,
        BigDecimal percentage) {
}
