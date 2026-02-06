package com.sergiocodev.app.dto.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank(message = "Brand name is required") @Size(min = 2, max = 150, message = "Brand name must be between 2 and 150 characters") String name,

        Boolean active) {
}
