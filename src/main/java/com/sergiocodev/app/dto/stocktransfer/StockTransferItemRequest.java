package com.sergiocodev.app.dto.stocktransfer;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StockTransferItemRequest(
        @NotNull(message = "Product ID is required") Long productId,
        @NotNull(message = "Lot ID is required") Long lotId,
        @NotNull(message = "Unit ID is required") Long unitId,
        @NotNull(message = "Quantity is required") BigDecimal quantity) {
}
