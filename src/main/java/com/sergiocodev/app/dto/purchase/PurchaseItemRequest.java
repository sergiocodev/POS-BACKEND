package com.sergiocodev.app.dto.purchase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseItemRequest(
        @NotNull(message = "Product ID is required") Long productId,

        @NotBlank(message = "Lot code is required") @Size(max = 100, message = "Lot code cannot exceed 100 characters") String lotCode,

        @NotNull(message = "Expiry date is required") LocalDate expiryDate,

        @NotNull(message = "Quantity is required") Integer quantity,

        Integer bonusQuantity,

        @NotNull(message = "Unit cost is required") BigDecimal unitCost) {

    public PurchaseItemRequest {
        if (bonusQuantity == null) {
            bonusQuantity = 0;
        }
    }
}
