package com.sergiocodev.app.dto.cash;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CloseSessionRequest(
        @NotNull(message = "User ID is required") Long userId,
        @NotNull(message = "Closing balance is required") BigDecimal closingBalance,
        String notes) {
}
