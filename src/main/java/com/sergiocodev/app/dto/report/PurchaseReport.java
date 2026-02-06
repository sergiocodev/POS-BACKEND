package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseReport(
        Long purchaseId,
        String supplierName,
        String documentType,
        String documentNumber,
        LocalDate issueDate,
        LocalDateTime arrivalDate,
        BigDecimal subTotal,
        BigDecimal tax,
        BigDecimal total,
        String status,
        Integer itemCount) {
}
