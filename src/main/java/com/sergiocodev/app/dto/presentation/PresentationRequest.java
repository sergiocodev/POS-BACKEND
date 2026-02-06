package com.sergiocodev.app.dto.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PresentationRequest(
        @NotBlank(message = "Description is required") @Size(max = 100, message = "Description cannot exceed 100 characters") String description) {
}
