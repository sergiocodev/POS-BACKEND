package com.sergiocodev.app.dto.product;

import java.util.List;

public record ProductResponse(
        Long id,
        String code,
        String digemidCode,
        String name,
        String description,
        String imageUrl,
        String brandName,
        String categoryName,
        String laboratoryName,
        String presentationDescription,
        String taxTypeName,
        boolean requiresPrescription,
        boolean isGeneric,
        String unitType,
        Integer purchaseFactor,
        String fractionLabel,
        boolean active,
        List<ProductIngredientResponse> ingredients) {
}
