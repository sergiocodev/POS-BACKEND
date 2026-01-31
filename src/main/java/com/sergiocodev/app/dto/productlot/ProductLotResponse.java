package com.sergiocodev.app.dto.productlot;

import com.sergiocodev.app.model.ProductLot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductLotResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String lotCode;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;

    public ProductLotResponse(ProductLot lot) {
        this.id = lot.getId();
        this.productId = lot.getProduct().getId();
        this.productName = lot.getProduct().getName();
        this.lotCode = lot.getLotCode();
        this.expiryDate = lot.getExpiryDate();
        this.createdAt = lot.getCreatedAt();
    }
}
