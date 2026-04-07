package com.sergiocodev.app.dto.stocktransfer;

import java.math.BigDecimal;

public record StockTransferItemResponse(
        Long id,
        Long stockTransferId,
        Long productId,
        String productName,
        Long lotId,
        String lotCode,
        Long unitId,
        String unitName,
        Integer unitFactor,
        BigDecimal quantity) {
}
