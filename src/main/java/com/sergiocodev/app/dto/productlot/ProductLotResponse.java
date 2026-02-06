package com.sergiocodev.app.dto.productlot;

import com.sergiocodev.app.model.ProductLot;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductLotResponse(
        Long id,
        Long productId,
        String productName,
        String lotCode,
        LocalDate expiryDate,
        LocalDateTime createdAt) {
    public ProductLotResponse(ProductLot lot) {
        this(
                lot.getId(),
                lot.getProduct().getId(),
                lot.getProduct().getName(),
                lot.getLotCode(),
                lot.getExpiryDate(),
                lot.getCreatedAt());
    }
}
