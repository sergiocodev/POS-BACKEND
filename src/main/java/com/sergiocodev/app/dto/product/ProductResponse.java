package com.sergiocodev.app.dto.product;

import java.util.List;

public record ProductResponse(
                Long id,
                String code,
                String barcode,
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
                String unitType,
                Integer purchaseFactor,
                String fractionLabel,
                boolean active,
                List<ProductIngredientResponse> ingredients,
                List<String> therapeuticActionNames, // Mapped from Product.therapeuticActions
                List<Long> therapeuticActionIds) {
}
