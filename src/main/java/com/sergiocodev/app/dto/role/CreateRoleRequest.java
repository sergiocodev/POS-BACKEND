package com.sergiocodev.app.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank(message = "El nombre del rol es obligatorio") @Size(max = 50, message = "El nombre no puede exceder 50 caracteres") String name,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres") String description,

        boolean active) {

    public CreateRoleRequest {
        if (active == false && name != null) {
            // Default active to true if not specified in some way?
            // Actually, records don't have default values in the same way.
            // But usually we can just pass true from the caller.
        }
    }
}
