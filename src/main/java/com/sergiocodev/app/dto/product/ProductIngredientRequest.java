package com.sergiocodev.app.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredientRequest {

    @NotNull(message = "Active ingredient ID is required")
    private Long activeIngredientId;

    @Size(max = 50, message = "Concentration cannot exceed 50 characters")
    private String concentration;
}
