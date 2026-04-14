package com.sergiocodev.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRequest(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be 3-50 characters") String username,

        @NotBlank(message = "Full name is required") @Size(max = 200, message = "Full name max 200 characters") String fullName,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 100, message = "Email max 100 characters") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,

        String profilePicture,

        Set<Long> roleIds) {
}
