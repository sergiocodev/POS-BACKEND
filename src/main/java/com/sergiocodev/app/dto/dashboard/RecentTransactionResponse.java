package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentTransactionResponse(
        Long id,
        String entityName,
        String initials,
        String transactionType, // "VENTA" or "COMPRA"
        String documentType,
        int productCount,
        LocalDateTime date,
        BigDecimal totalAmount) {
}
