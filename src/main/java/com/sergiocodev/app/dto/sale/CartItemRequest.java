package com.sergiocodev.app.dto.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CartItemRequest(
        @NotNull(message = "Product ID is required") Long productId,

        Long lotId,

        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity,

        @NotNull(message = "Unit price is required") @Positive(message = "Unit price must be positive") BigDecimal unitPrice,

        @NotNull(message = "Product unit ID is required") Long productUnitId,

        @PositiveOrZero(message = "Discount must be zero or positive") BigDecimal discountAmount,

        @Size(max = 100, message = "Discount reason max 100 characters") String discountReason) {
}
