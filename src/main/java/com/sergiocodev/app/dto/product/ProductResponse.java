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
        Long categoryId,
        String categoryName,
        Long laboratoryId,
        String laboratoryName,
        Long presentationId,
        String presentationDescription,
        Long taxTypeId,
        String taxTypeName,
        java.math.BigDecimal taxTypeRate,
        Long pharmaceuticalFormId,
        String pharmaceuticalFormName,
        boolean requiresPrescription,
        boolean isGeneric,
        boolean isActive,
        List<ProductIngredientResponse> ingredients,
        List<String> therapeuticActionNames, // Mapped from Product.therapeuticActions
        List<Long> therapeuticActionIds,
        List<ProductUnitResponse> units) {
}
