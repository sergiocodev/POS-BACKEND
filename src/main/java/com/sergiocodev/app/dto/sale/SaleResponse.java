package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.Sale;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        String establishmentName,
        String customerName,
        String username,
        Sale.SaleDocumentType documentType,
        String series,
        String number,
        LocalDateTime date,
        BigDecimal subTotal,
        BigDecimal tax,
        BigDecimal total,
        Sale.SaleStatus status,
        Sale.SunatStatus sunatStatus,
        String pdfUrl,
        String cdrUrl,

        // Campos de NC/ND
        Long relatedSaleId,
        String noteCode,
        String noteReason,

        // Campos de anulación
        boolean voided,
        LocalDateTime voidedAt,
        String voidReason,

        List<SaleItemResponse> items,
        List<SalePaymentResponse> payments) {
}
