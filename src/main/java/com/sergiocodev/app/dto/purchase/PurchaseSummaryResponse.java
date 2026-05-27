package com.sergiocodev.app.dto.purchase;

import java.math.BigDecimal;

public record PurchaseSummaryResponse(
    BigDecimal totalFacturas,
    BigDecimal totalBoletas,
    BigDecimal totalGuiaRemision,
    BigDecimal totalNeto
) {
    public static PurchaseSummaryResponse empty() {
        return new PurchaseSummaryResponse(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
            BigDecimal.ZERO
        );
    }
}
