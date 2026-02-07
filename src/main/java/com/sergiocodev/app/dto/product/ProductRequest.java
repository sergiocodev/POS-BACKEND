package com.sergiocodev.app.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductRequest(
                @NotBlank(message = "Code is required") @Size(max = 50, message = "Code cannot exceed 50 characters") String code,

                @Size(max = 50, message = "Barcode cannot exceed 50 characters") String barcode,

                @Size(max = 50, message = "DIGEMID code cannot exceed 50 characters") String digemidCode,

                @NotBlank(message = "Trade Name is required") @Size(max = 255, message = "Trade Name cannot exceed 255 characters") String tradeName,

                @Size(max = 255, message = "Generic Name cannot exceed 255 characters") String genericName,

                String description,

                @Size(max = 255, message = "Image URL cannot exceed 255 characters") String imageUrl,

                @NotNull(message = "Brand ID is required") Long brandId,

                @NotNull(message = "Category ID is required") Long categoryId,

                @NotNull(message = "Laboratory ID is required") Long laboratoryId,

                @NotNull(message = "Presentation ID is required") Long presentationId,

                @NotNull(message = "Tax type ID is required") Long taxTypeId,

                @NotNull(message = "Pharmaceutical form ID is required") Long pharmaceuticalFormId,

                boolean requiresPrescription,
                boolean isGeneric,

                @NotBlank(message = "Unit type is required") String unitType,

                @NotNull(message = "Purchase factor is required") Integer purchaseFactor,

                String fractionLabel,
                boolean active,

                List<ProductIngredientRequest> ingredients,

                List<Long> therapeuticActionIds) {
}
