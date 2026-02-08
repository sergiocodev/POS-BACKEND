package com.sergiocodev.app.dto.sale;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CartItemRequest(
        @NotNull Long productId,
        Long lotId,
        @NotNull BigDecimal quantity,
        @NotNull BigDecimal unitPrice,
        BigDecimal discount) {
}
