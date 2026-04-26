package com.sergiocodev.app.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductForSaleResponse(
        Long id,
        Long productId,
        Long productUnitId,
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
        Long lotId,
        String imageUrl,
        String barcode,
        String locationShelf,
        String unitName,
        Integer factor,
        BigDecimal taxRate) {
}
