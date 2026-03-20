package com.sergiocodev.app.dto.productunit;

import java.math.BigDecimal;

public record ProductUnitResponse(
        Long id,
        Long productId,
        String unitName,
        Integer factor,
        String barcode,
        String sunatCode,
        BigDecimal price,
        boolean isBaseUnit) {
}
