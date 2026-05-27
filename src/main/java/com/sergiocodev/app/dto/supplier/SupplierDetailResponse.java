package com.sergiocodev.app.dto.supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplierDetailResponse(
        Long id,
        String name,
        String ruc,
        String category,
        String contactName,
        String email,
        com.sergiocodev.app.model.Supplier.SupplierStatus status,
        BigDecimal rating,
        LocalDateTime lastPurchase,
        BigDecimal purchaseVolume
) {}
