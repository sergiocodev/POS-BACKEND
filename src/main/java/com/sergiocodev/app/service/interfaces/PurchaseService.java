package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.purchase.PurchaseRequest;
import com.sergiocodev.app.dto.purchase.PurchaseResponse;
import com.sergiocodev.app.dto.purchase.PurchaseSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseService {
    PurchaseResponse create(PurchaseRequest request, Long userId);

    List<PurchaseResponse> getAll();

    Page<PurchaseResponse> getAllPaged(
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
            String columnDate,
            Pageable pageable);

    PurchaseSummaryResponse getSummary(
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

    PurchaseResponse getById(Long id);

    void cancel(Long id);
}
