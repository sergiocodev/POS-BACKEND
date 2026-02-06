package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LowRotationReport(
        Long productId,
        String productName,
        LocalDateTime lastSaleDate,
        BigDecimal currentStock) {
}
