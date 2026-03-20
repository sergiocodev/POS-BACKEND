package com.sergiocodev.app.dto.product;

import java.util.List;

public record ProductResponse(
        Long id,
        String code,
        String digemidCode,
        String tradeName,
        String genericName,
        String description,
        String imageUrl,
        String brandName,
        String categoryName,
        String laboratoryName,
        String presentationDescription,
        String taxTypeName,
        String pharmaceuticalFormName,
        boolean requiresPrescription,
        boolean isGeneric,
        List<ProductIngredientResponse> ingredients,
        List<String> therapeuticActionNames, // Mapped from Product.therapeuticActions
        List<Long> therapeuticActionIds) {
}
