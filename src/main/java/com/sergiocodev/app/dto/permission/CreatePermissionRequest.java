package com.sergiocodev.app.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;

    @Size(max = 50, message = "El módulo no puede exceder 50 caracteres")
    private String module;
}
