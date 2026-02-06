package com.sergiocodev.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UserRequest(
        @NotBlank(message = "Username is required") String username,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        String fullName,
        String profilePicture,
        String password,
        Boolean active,
        Set<Long> roleIds) {
}
