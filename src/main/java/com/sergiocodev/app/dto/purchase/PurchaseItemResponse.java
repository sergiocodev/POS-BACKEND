package com.sergiocodev.app.dto.purchase;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseItemResponse(
    Long id,
    String productName,
    Long productUnitId,
    String unitName,
    Integer factor,
    String lotCode,
    LocalDate expiryDate,
    Integer quantity,
    Integer bonusQuantity,
    BigDecimal unitCost,
    BigDecimal totalCost
) {}
