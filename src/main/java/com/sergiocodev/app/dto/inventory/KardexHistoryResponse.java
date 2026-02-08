package com.sergiocodev.app.dto.inventory;

import com.sergiocodev.app.model.StockMovement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KardexHistoryResponse(
        Long id,
        Long establishmentId,
        String establishmentName,
        Long productId,
        String productName,
        Long lotId,
        String lotCode,
        StockMovement.MovementType type,
        BigDecimal quantity,
        BigDecimal balanceAfter,
        String referenceTable,
        Long referenceId,
        String userName,
        LocalDateTime createdAt) {
}
