package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseDebtReport(
        Long purchaseId,
        LocalDate issueDate,
        String supplierName,
        String documentType,
        String documentNumber,
        String paymentCondition, // CONTADO, CREDITO
        String paymentMethod,    // Efectivo, Transferencia
        LocalDate paymentDate,   // Cuando se pago si esta cancelado
        Integer creditDays,      // Dias de credito
        LocalDate dueDate,       // Fecha de vencimiento
        Integer overdueDays,     // Dias de mora
        BigDecimal total,        // Monto total de compra
        BigDecimal pending,      // Monto pendiente
        String reportGroup       // CANCELADO, CREDITO, VENCIDO
) {
}
