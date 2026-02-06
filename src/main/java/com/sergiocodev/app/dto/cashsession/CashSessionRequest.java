package com.sergiocodev.app.dto.cashsession;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CashSessionRequest(
        @NotNull(message = "Cash register ID is required") Long cashRegisterId,

        @NotNull(message = "Opening balance is required") BigDecimal openingBalance,

        String notes) {
}
