package com.sergiocodev.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.sergiocodev.app.validation.ValidRoleIds;
import lombok.Data;

import java.util.Set;

@Data
public class UserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name max 200 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email max 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String profilePicture;

    @NotNull(message = "La lista de roles es obligatoria")
    @NotEmpty(message = "Se requiere al menos un rol")
    @ValidRoleIds
    private Set<Long> roleIds;

    // Constructor por defecto
    public UserRequest() {
    }

    // Constructor con todos los campos
    public UserRequest(
        String username,
        String fullName,
        String email,
        String password,
        String profilePicture,
        Set<Long> roleIds
    ) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.profilePicture = profilePicture;
        this.roleIds = roleIds;
    }
}