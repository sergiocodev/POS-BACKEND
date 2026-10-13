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
        BigDecimal taxRate,
        java.util.List<com.sergiocodev.app.dto.product.ProductIngredientResponse> ingredients,
        String pharmaceuticalFormName) {
}
