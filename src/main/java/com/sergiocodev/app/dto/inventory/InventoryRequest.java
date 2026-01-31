package com.sergiocodev.app.dto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    @NotNull(message = "Lot ID is required")
    private Long lotId;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    private BigDecimal costPrice;
    private BigDecimal salesPrice;
}
