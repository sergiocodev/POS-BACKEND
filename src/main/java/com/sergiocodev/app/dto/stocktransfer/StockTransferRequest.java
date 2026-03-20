package com.sergiocodev.app.dto.stocktransfer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record StockTransferRequest(
        @NotNull(message = "Source establishment ID is required") Long sourceEstablishmentId,
        @NotNull(message = "Target establishment ID is required") Long targetEstablishmentId,
        String notes,
        @NotEmpty(message = "Must transfer at least one item") List<StockTransferItemRequest> items) {
}
