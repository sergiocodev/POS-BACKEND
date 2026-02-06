package com.sergiocodev.app.dto.taxtype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TaxTypeRequest(
        @NotBlank(message = "Name is required") @Size(max = 50, message = "Name cannot exceed 50 characters") String name,

        @NotNull(message = "Rate is required") BigDecimal rate,

        @Size(max = 10, message = "SUNAT code cannot exceed 10 characters") String codeSunat,

        boolean active) {
}
