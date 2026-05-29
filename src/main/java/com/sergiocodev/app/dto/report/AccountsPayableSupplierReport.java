package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountsPayableSupplierReport(
        Long supplierId,
        String supplierName,
        BigDecimal totalPending,
        int pendingInvoicesCount,
        BigDecimal overdueDebt,
        List<InvoiceDetail> invoices) {

    public record InvoiceDetail(
            String invoiceNumber,
            LocalDate purchaseDate,
            LocalDate dueDate,
            BigDecimal pendingAmount,
            String status) {
    }
}
