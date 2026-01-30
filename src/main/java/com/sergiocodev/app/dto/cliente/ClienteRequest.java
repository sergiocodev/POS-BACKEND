package com.sergiocodev.app.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest {

    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 20, message = "El DNI no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9]+$", message = "El DNI debe contener solo números")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 3, max = 100, message = "Los nombres deben tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;
}
