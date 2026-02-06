package com.sergiocodev.app.dto.activeingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActiveIngredientRequest(
        @NotBlank(message = "Name is required") @Size(max = 200, message = "Name cannot exceed 200 characters") String name,

        String description,
        boolean active) {
}
