package com.sergiocodev.app.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long establishmentId,
        String establishmentName,
        Long lotId,
        String lotCode,
        String productName,
        BigDecimal quantity,
        BigDecimal costPrice,
        BigDecimal salesPrice,
        LocalDateTime lastMovement) {
}
