package com.sergiocodev.app.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Application event published whenever stock changes.
 * Useful for audit trail, notifications, and downstream integrations.
 */
@Getter
public class StockMovementEvent extends ApplicationEvent {

    public enum MovementType {
        SALE, PURCHASE, ADJUSTMENT, TRANSFER, REVERSAL
    }

    private final Long establishmentId;
    private final Long productId;
    private final Long lotId;
    private final MovementType movementType;
    private final BigDecimal quantity;
    private final BigDecimal balanceAfter;
    private final String reason;
    private final Long referenceId;
    private final String referenceTable;
    private final Long userId;
    private final LocalDateTime occurredAt;

    public StockMovementEvent(Object source, Long establishmentId, Long productId, Long lotId,
                               MovementType movementType, BigDecimal quantity, BigDecimal balanceAfter,
                               String reason, Long referenceId, String referenceTable, Long userId) {
        super(source);
        this.establishmentId = establishmentId;
        this.productId = productId;
        this.lotId = lotId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.referenceId = referenceId;
        this.referenceTable = referenceTable;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }
}
