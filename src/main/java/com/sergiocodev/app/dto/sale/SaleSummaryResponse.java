package com.sergiocodev.app.dto.sale;

import java.math.BigDecimal;

public record SaleSummaryResponse(
    BigDecimal totalFacturas,
    BigDecimal totalBoletas,
    BigDecimal totalNotaCredito,
    BigDecimal totalNotaDebito,
    BigDecimal totalNotaVenta,
    BigDecimal totalNeto
) {
    public static SaleSummaryResponse empty() {
        return new SaleSummaryResponse(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
    }
}
