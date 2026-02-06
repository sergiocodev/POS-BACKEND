package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesReport(
        Long saleId,
        String customerName,
        String employeeName,
        String documentType,
        String documentNumber,
        LocalDateTime date,
        BigDecimal subTotal,
        BigDecimal tax,
        BigDecimal total,
        String status,
        String sunatStatus,
        boolean isVoided) {
}
