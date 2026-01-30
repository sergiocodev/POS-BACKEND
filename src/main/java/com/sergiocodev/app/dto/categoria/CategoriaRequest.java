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
    @Size(min = 2, max = 100, message = "El nombre de la categoría debe tener entre 2 y 100 caracteres")
    private String nombreCategoria;
}
