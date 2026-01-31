package com.sergiocodev.app.dto.stockmovement;

import com.sergiocodev.app.model.StockMovement;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementRequest {

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long lotId;

    @NotNull(message = "Movement type is required")
    private StockMovement.MovementType type;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    private String reason;
    private Long referenceId;
    private StockMovement.ReferenceType referenceType;
}
