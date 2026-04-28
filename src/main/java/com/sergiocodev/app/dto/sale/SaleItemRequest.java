package com.sergiocodev.app.dto.sale;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaleItemRequest(
                @NotNull(message = "Product ID is required") Long productId,

                @NotNull(message = "Lot ID is required") Long lotId,

                @NotNull(message = "Product unit ID is required") Long productUnitId,

                @NotNull(message = "Quantity is required") BigDecimal quantity,

                @NotNull(message = "Unit price is required") BigDecimal unitPrice,
                
                BigDecimal discountAmount,
                String discountReason,
                BigDecimal increaseAmount,
                String increaseReason) {
}
