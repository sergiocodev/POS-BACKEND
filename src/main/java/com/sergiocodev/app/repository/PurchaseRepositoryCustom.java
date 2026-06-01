package com.sergiocodev.app.repository;

import com.sergiocodev.app.dto.purchase.PurchaseSummaryResponse;

import java.time.LocalDateTime;

public interface PurchaseRepositoryCustom {

    PurchaseSummaryResponse getPurchaseSummary(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String documentType,
            String series,
            String number,
            String supplierName,
            String supplierDocument,
            String userName,
            String status,
            String total,
            String paymentMethod,
            String columnDate);
}
