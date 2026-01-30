package com.sergiocodev.app.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String usuario;
    private String nombre;

    public LoginResponse(String token, Long id, String usuario, String nombre) {
        this.token = token;
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
    }
}
