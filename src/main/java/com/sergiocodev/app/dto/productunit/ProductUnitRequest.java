package com.sergiocodev.app.dto.productunit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductUnitRequest(
        @NotNull(message = "Product ID is required") Long productId,
        @NotBlank(message = "Unit name is required") String unitName,
        @NotNull(message = "Factor is required") Integer factor,
        String barcode,
        String sunatCode,
        @NotNull(message = "Price is required") BigDecimal price,
        boolean isBaseUnit) {
}
