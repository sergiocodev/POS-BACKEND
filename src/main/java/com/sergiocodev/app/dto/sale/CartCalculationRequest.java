package com.sergiocodev.app.dto.sale;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CartCalculationRequest(
        @NotNull Long establishmentId,
        List<CartItemRequest> items,
        BigDecimal globalDiscount) {
}
