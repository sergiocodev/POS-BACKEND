package com.sergiocodev.app.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductIngredientRequest(
        @NotNull(message = "Active ingredient ID is required") Long activeIngredientId,

        @Size(max = 50, message = "Concentration cannot exceed 50 characters") String concentration) {
}
