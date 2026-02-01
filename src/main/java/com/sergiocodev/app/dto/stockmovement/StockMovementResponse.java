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
    private BigDecimal balanceAfter;
    private String referenceTable;
    private Long referenceId;
    private String userName;
    private LocalDateTime createdAt;

    public StockMovementResponse(StockMovement sm) {
        this.id = sm.getId();
        this.establishmentName = sm.getEstablishment().getName();
        this.productName = sm.getLot() != null && sm.getLot().getProduct() != null
                ? sm.getLot().getProduct().getName()
                : null;
        this.lotCode = sm.getLot() != null ? sm.getLot().getLotCode() : null;
        this.type = sm.getType();
        this.quantity = sm.getQuantity();
        this.balanceAfter = sm.getBalanceAfter();
        this.referenceTable = sm.getReferenceTable();
        this.referenceId = sm.getReferenceId();
        this.userName = sm.getUser() != null ? sm.getUser().getUsername() : null;
        this.createdAt = sm.getCreatedAt();
    }
}
