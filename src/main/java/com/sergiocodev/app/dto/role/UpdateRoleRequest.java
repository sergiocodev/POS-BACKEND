package com.sergiocodev.app.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @NotBlank(message = "El nombre del rol es obligatorio") @Size(max = 50, message = "El nombre no puede exceder 50 caracteres") String name,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres") String description,

        boolean active) {
}
