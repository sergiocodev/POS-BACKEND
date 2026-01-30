package com.sergiocodev.app.dto.marca;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcaRequest {

    @NotBlank(message = "El nombre de la marca es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre de la marca debe tener entre 2 y 100 caracteres")
    private String nombreMarca;
}
