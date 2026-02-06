package com.sergiocodev.app.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank(message = "El nombre del permiso es obligatorio") @Size(max = 100, message = "El nombre no puede exceder 100 caracteres") String name,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres") String description,

        @Size(max = 50, message = "El módulo no puede exceder 50 caracteres") String module) {
}
