package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.PurchaseItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemResponse {

    private Long id;
    private String productName;
    private String lotCode;
    private LocalDate expiryDate;
    private Integer quantity;
    private Integer bonusQuantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;

    public PurchaseItemResponse(PurchaseItem item) {
        this.id = item.getId();
        this.productName = item.getProduct().getName();
        this.lotCode = item.getLotCode();
        this.expiryDate = item.getExpiryDate();
        this.quantity = item.getQuantity();
        this.bonusQuantity = item.getBonusQuantity();
        this.unitCost = item.getUnitCost();
        this.totalCost = item.getTotalCost();
    }
}
