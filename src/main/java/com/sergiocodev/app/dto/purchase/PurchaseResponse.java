package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.Purchase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponse(
    Long id,
    String supplierName,
    String establishmentName,
    String username,
    Purchase.PurchaseDocumentType documentType,
    String series,
    String number,
    LocalDate issueDate,
    LocalDateTime arrivalDate,
    BigDecimal subTotal,
    BigDecimal tax,
    BigDecimal total,
    Purchase.PurchaseStatus status,
    Purchase.PaymentCondition paymentCondition,
    String notes,
    LocalDateTime createdAt,
    List<PurchaseItemResponse> items,
    com.sergiocodev.app.dto.company.CompanyMinimalResponse company
) {}
