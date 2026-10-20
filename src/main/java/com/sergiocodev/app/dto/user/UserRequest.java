package com.sergiocodev.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.sergiocodev.app.validation.ValidRoleIds;
import java.util.Set;

public record UserRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    String username,

    @Size(max = 200, message = "Full name max 200 characters")
    String fullName,

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email max 100 characters")
    String email,

    @Size(max = 30, message = "Celular max 30 characters")
    String celular,

    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,

    String profilePicture,

    @ValidRoleIds
    Set<Long> roleIds
) {}