package com.sergiocodev.app.dto.cashregister;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CashRegisterRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name cannot exceed 100 characters") String name,

        @NotNull(message = "Establishment ID is required") Long establishmentId,

        Boolean active) {
    public CashRegisterRequest {
        if (active == null) {
            active = true;
        }
    }
}
