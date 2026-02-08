package com.sergiocodev.app.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductSearchResponse(
        Long id,
        Long productId,
        String tradeName,
        String genericName,
        String description,
        String presentation,
        String concentration,
        String category,
        String laboratory,
        BigDecimal salesPrice,
        BigDecimal stock,
        LocalDate expirationDate,
        String lotCode,
        Long lotId) {
}
