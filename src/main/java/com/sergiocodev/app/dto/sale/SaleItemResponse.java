package com.sergiocodev.app.dto.sale;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long id,
        String productName,
        String lotCode,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount) {
}
