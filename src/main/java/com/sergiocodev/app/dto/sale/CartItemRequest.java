package com.sergiocodev.app.dto.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CartItemRequest(
                @NotNull(message = "Product ID is required") Long productId,

                Long lotId,

                @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity,

                @NotNull(message = "Unit price is required") @Positive(message = "Unit price must be positive") BigDecimal unitPrice,

                @NotNull(message = "Product unit ID is required") Long productUnitId,

                BigDecimal discountAmount,

                String discountReason,
                BigDecimal increaseAmount,
                String increaseReason) {
}
