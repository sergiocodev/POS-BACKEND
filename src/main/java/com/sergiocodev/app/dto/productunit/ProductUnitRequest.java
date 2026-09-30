package com.sergiocodev.app.dto.productunit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductUnitRequest(
        @NotNull(message = "Product ID is required") Long productId,
        @NotNull(message = "Unit of Measure is required") Long unitOfMeasureId,
        @NotNull(message = "Factor is required") Integer factor,
        String barcode,
        String sunatCode,
        @NotNull(message = "Price is required") BigDecimal price,
        boolean isBaseUnit) {
}
