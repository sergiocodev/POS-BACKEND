package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.SaleItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponse {

    private Long id;
    private String productName;
    private String lotCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;

    public SaleItemResponse(SaleItem item) {
        this.id = item.getId();
        this.productName = item.getProduct().getName();
        this.lotCode = item.getLot() != null ? item.getLot().getLotCode() : null;
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.amount = item.getAmount();
    }
}
