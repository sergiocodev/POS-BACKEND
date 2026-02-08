package com.sergiocodev.app.dto.sale;

import java.math.BigDecimal;

public record CartItemCalculation(
        Long productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal taxAmount,
        BigDecimal lineTotal) {
}
