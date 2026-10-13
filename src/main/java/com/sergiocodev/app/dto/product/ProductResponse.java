package com.sergiocodev.app.dto.product;

import java.util.List;
import com.sergiocodev.app.dto.productunit.ProductUnitResponse;

public record ProductResponse(
        Long id,
        String code,
        String digemidCode,
        String tradeName,
        String genericName,
        String description,
        String sanitaryRegistration,
        String imageUrl,
        String categoryName,
        String laboratoryName,
        String presentationDescription,
        String taxTypeName,
        java.math.BigDecimal taxTypeRate,
        String pharmaceuticalFormName,
        boolean requiresPrescription,
        boolean isGeneric,
        List<ProductIngredientResponse> ingredients,
        List<String> therapeuticActionNames, // Mapped from Product.therapeuticActions
        List<Long> therapeuticActionIds,
        List<ProductUnitResponse> units) {
}
