package com.sergiocodev.app.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpiringLotResponse(
        Long inventoryId,
        Long productId,
        String productName,
        String barcode,
        Long lotId,
        String lotCode,
        LocalDate expiryDate,
        long daysUntilExpiration,
        BigDecimal quantity,
        Long establishmentId,
        String establishmentName,
        String alertLevel // CRITICAL, WARNING, INFO
) {
}
