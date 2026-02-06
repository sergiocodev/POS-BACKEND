package com.sergiocodev.app.dto.stockmovement;

import com.sergiocodev.app.model.StockMovement;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StockMovementRequest(
        @NotNull(message = "Establishment ID is required") Long establishmentId,

        @NotNull(message = "Lot ID is required") Long lotId,

        @NotNull(message = "Movement type is required") StockMovement.MovementType type,

        @NotNull(message = "Quantity is required") BigDecimal quantity,

        @NotNull(message = "Balance after is required") BigDecimal balanceAfter,

        String referenceTable,
        Long referenceId,
        Long userId) {
}
