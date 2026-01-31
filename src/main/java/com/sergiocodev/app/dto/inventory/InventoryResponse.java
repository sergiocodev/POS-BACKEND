package com.sergiocodev.app.dto.inventory;

import com.sergiocodev.app.model.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long establishmentId;
    private String establishmentName;
    private Long lotId;
    private String lotCode;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal costPrice;
    private BigDecimal salesPrice;
    private LocalDateTime lastMovement;

    public InventoryResponse(Inventory inventory) {
        this.id = inventory.getId();
        this.establishmentId = inventory.getEstablishment().getId();
        this.establishmentName = inventory.getEstablishment().getName();
        this.lotId = inventory.getLot().getId();
        this.lotCode = inventory.getLot().getLotCode();
        this.productName = inventory.getLot().getProduct().getName();
        this.quantity = inventory.getQuantity();
        this.costPrice = inventory.getCostPrice();
        this.salesPrice = inventory.getSalesPrice();
        this.lastMovement = inventory.getLastMovement();
    }
}
