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
    private String username;
    private String email;
    private String fullName;
    private java.util.Set<String> roles;
    private java.util.Set<String> permissions;

    public LoginResponse(String token, Long id, String username, String email, String fullName,
            java.util.Set<String> roles, java.util.Set<String> permissions) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.permissions = permissions;
    }
}
