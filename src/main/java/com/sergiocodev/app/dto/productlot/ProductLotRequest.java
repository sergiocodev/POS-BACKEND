package com.sergiocodev.app.dto.productlot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProductLotRequest(
        @NotNull(message = "Product ID is required") Long productId,

        @NotBlank(message = "Lot code is required") @Size(max = 100, message = "Lot code cannot exceed 100 characters") String lotCode,

        @NotNull(message = "Expiry date is required") LocalDate expiryDate) {
}
