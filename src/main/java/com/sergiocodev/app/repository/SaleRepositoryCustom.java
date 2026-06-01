package com.sergiocodev.app.repository;

import com.sergiocodev.app.dto.sale.SaleSummaryResponse;

import java.time.LocalDateTime;

public interface SaleRepositoryCustom {

    SaleSummaryResponse getSaleSummary(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String documentType,
            String series,
            String number,
            String customerName,
            String customerDocument,
            String vendedorName,
            String status,
            String sunatStatus,
            String total,
            String paymentMethod,
            String columnDate);
}
