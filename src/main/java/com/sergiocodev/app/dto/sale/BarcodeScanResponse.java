package com.sergiocodev.app.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BarcodeScanResponse(
        Long productId,
        String tradeName,
        String barcode,
        BigDecimal salesPrice,
        Long lotId,
        String lotCode,
        LocalDate expiryDate,
        BigDecimal availableStock,
        String message) {
}
