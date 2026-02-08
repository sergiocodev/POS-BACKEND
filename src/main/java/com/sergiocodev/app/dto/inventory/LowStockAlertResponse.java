package com.sergiocodev.app.dto.inventory;

import java.math.BigDecimal;

public record LowStockAlertResponse(
        Long inventoryId,
        Long productId,
        String productName,
        String barcode,
        String code,
        BigDecimal quantity,
        Integer minStock,
        BigDecimal difference,
        Long establishmentId,
        String establishmentName,
        Long lotId,
        String lotCode) {
}
