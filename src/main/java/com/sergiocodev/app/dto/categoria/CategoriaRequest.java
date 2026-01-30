package com.sergiocodev.app.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre de la categoría debe tener entre 2 y 150 caracteres")
    private String name;

    private Boolean active = true;
}
