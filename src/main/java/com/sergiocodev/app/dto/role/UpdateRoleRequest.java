package com.sergiocodev.app.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;

    private boolean active;
}
