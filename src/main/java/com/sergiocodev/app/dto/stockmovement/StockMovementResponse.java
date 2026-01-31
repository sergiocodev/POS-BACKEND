package com.sergiocodev.app.dto.stockmovement;

import com.sergiocodev.app.model.StockMovement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private Long id;
    private String establishmentName;
    private String productName;
    private String lotCode;
    private StockMovement.MovementType type;
    private BigDecimal quantity;
    private String reason;
    private Long referenceId;
    private StockMovement.ReferenceType referenceType;
    private LocalDateTime createdAt;

    public StockMovementResponse(StockMovement sm) {
        this.id = sm.getId();
        this.establishmentName = sm.getEstablishment().getName();
        this.productName = sm.getProduct().getName();
        this.lotCode = sm.getLot() != null ? sm.getLot().getLotCode() : null;
        this.type = sm.getType();
        this.quantity = sm.getQuantity();
        this.reason = sm.getReason();
        this.referenceId = sm.getReferenceId();
        this.referenceType = sm.getReferenceType();
        this.createdAt = sm.getCreatedAt();
    }
}
