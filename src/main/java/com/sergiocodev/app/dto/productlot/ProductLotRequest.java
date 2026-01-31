package com.sergiocodev.app.dto.productlot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductLotRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Lot code is required")
    @Size(max = 100, message = "Lot code cannot exceed 100 characters")
    private String lotCode;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
}
