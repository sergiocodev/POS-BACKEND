package com.sergiocodev.app.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank(message = "First name is required") @Size(max = 100, message = "First name cannot exceed 100 characters") String firstName,

        @Size(max = 100, message = "Last name cannot exceed 100 characters") String lastName,

        @Size(max = 20, message = "Document number cannot exceed 20 characters") String documentNumber,

        Long userId,
        Boolean active) {
    public EmployeeRequest {
        if (active == null) {
            active = true;
        }
    }
}
