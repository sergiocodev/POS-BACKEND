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

    @NotNull(message = "Lot ID is required")
    private Long lotId;

    @NotNull(message = "Movement type is required")
    private StockMovement.MovementType type;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotNull(message = "Balance after is required")
    private BigDecimal balanceAfter;

    private String referenceTable;
    private Long referenceId;
    private Long userId;
}
