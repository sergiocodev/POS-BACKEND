package com.sergiocodev.app.dto.cash;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OpenDailySessionRequest(
        @NotNull(message = "Cash register ID is required") Long cashRegisterId,
        @NotNull(message = "Opening balance is required") BigDecimal openingBalance,
        @NotNull(message = "User ID is required") Long userId,
        String notes) {
}
