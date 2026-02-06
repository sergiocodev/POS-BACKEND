package com.sergiocodev.app.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required") @Size(min = 2, max = 150, message = "Category name must be between 2 and 150 characters") String name,

        Boolean active) {
    public CategoryRequest {
        if (active == null) {
            active = true;
        }
    }
}
