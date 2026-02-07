package com.sergiocodev.app.dto.therapeuticaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TherapeuticActionRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name cannot exceed 100 characters") String name,

        @Size(max = 255, message = "Description cannot exceed 255 characters") String description,

        boolean active) {
}
