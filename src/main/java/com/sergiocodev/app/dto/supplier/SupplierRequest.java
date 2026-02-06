package com.sergiocodev.app.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "Name is required") @Size(max = 255, message = "Name cannot exceed 255 characters") String name,

        @Size(max = 20, message = "RUC cannot exceed 20 characters") String ruc,

        @Size(max = 30, message = "Phone cannot exceed 30 characters") String phone,

        @Size(max = 255, message = "Email cannot exceed 255 characters") String email,

        @Size(max = 255, message = "Address cannot exceed 255 characters") String address,

        Boolean active) {
    public SupplierRequest {
        if (active == null) {
            active = true;
        }
    }
}
