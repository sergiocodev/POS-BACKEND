package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;

public record SalesByCategoryResponse(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        double percentage) {
}
