package com.sergiocodev.app.dto.sale;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaleItemRequest(
        @NotNull(message = "Product ID is required") Long productId,

        Long lotId,

        @NotNull(message = "Quantity is required") BigDecimal quantity,

        @NotNull(message = "Unit price is required") BigDecimal unitPrice) {
}
