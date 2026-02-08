package com.sergiocodev.app.dto.cash;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CashOutflowRequest(
        @NotNull(message = "User ID is required") Long userId,
        @NotNull(message = "Amount is required") BigDecimal amount,
        @NotNull(message = "Concept ID is required") Long conceptId,
        String description) {
}
