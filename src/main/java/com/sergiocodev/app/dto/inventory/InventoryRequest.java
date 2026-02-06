package com.sergiocodev.app.dto.inventory;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventoryRequest(
        @NotNull(message = "Establishment ID is required") Long establishmentId,

        @NotNull(message = "Lot ID is required") Long lotId,

        @NotNull(message = "Quantity is required") BigDecimal quantity,

        BigDecimal costPrice,
        BigDecimal salesPrice,

        // For adjustments
        String movementType, // IN, OUT, ADJUSTMENT, LOSS, THEFT, RETURN
        String notes) {
}
