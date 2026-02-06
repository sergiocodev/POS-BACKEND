package com.sergiocodev.app.dto.stockmovement;

import com.sergiocodev.app.model.StockMovement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        String establishmentName,
        String productName,
        String lotCode,
        StockMovement.MovementType type,
        BigDecimal quantity,
        BigDecimal balanceAfter,
        String referenceTable,
        Long referenceId,
        String userName,
        LocalDateTime createdAt) {
    public StockMovementResponse(StockMovement sm) {
        this(
                sm.getId(),
                sm.getEstablishment().getName(),
                sm.getLot() != null && sm.getLot().getProduct() != null
                        ? sm.getLot().getProduct().getName()
                        : null,
                sm.getLot() != null ? sm.getLot().getLotCode() : null,
                sm.getType(),
                sm.getQuantity(),
                sm.getBalanceAfter(),
                sm.getReferenceTable(),
                sm.getReferenceId(),
                sm.getUser() != null ? sm.getUser().getUsername() : null,
                sm.getCreatedAt());
    }
}
