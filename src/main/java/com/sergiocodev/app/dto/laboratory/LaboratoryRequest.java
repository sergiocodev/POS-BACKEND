package com.sergiocodev.app.dto.laboratory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LaboratoryRequest(
        @NotBlank(message = "Name is required") @Size(max = 200, message = "Name cannot exceed 200 characters") String name,

        boolean active) {
}
