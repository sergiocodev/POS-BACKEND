package com.sergiocodev.app.dto.inventory;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StockAdjustmentRequest(
        @NotNull(message = "Product ID is required") Long productId,

        @NotNull(message = "Lot ID is required") Long lotId,

        @NotNull(message = "Establishment ID is required") Long establishmentId,

        @NotNull(message = "Actual quantity is required") BigDecimal actualQuantity,

        @NotNull(message = "Reason is required") String reason, // THEFT, BREAKAGE, COUNT_ERROR, EXPIRED, OTHER

        String notes,

        Long userId) {
}
