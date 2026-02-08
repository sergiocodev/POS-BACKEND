package com.sergiocodev.app.dto.sale;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CartCalculationResponse(
        BigDecimal subTotal,
        BigDecimal totalTax,
        BigDecimal totalDiscount,
        BigDecimal total,
        Map<String, BigDecimal> taxBreakdown, // key: tax name/type, value: amount
        List<CartItemCalculation> items) {
}
