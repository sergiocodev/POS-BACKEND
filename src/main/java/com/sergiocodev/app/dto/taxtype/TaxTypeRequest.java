package com.sergiocodev.app.dto.taxtype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;

    @NotNull(message = "Rate is required")
    private BigDecimal rate;

    @Size(max = 10, message = "SUNAT code cannot exceed 10 characters")
    private String codeSunat;

    private boolean active = true;
}
