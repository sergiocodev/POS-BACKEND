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

    @Size(max = 20, message = "El DNI no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9]*$", message = "El DNI debe contener solo números")
    private String dni;

    @Size(max = 20, message = "El RUC no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9]*$", message = "El RUC debe contener solo números")
    private String ruc;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String lastName;

    @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
    private String phone;

    @jakarta.validation.constraints.Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    private String address;
}
