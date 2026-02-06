package com.sergiocodev.app.dto.establishment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstablishmentRequest(
        @NotBlank(message = "Name is required") @Size(max = 255, message = "Name cannot exceed 255 characters") String name,

        @Size(max = 255, message = "Address cannot exceed 255 characters") String address,

        @Size(max = 10, message = "SUNAT code cannot exceed 10 characters") String codeSunat,

        Boolean active) {
    public EstablishmentRequest {
        if (codeSunat == null) {
            codeSunat = "0000";
        }
        if (active == null) {
            active = true;
        }
    }
}
