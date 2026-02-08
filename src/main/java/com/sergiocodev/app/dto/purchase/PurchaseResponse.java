package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.Purchase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long id;
    private String supplierName;
    private String establishmentName;
    private String username;
    private Purchase.PurchaseDocumentType documentType;
    private Purchase.PaymentMethod paymentMethod;
    private String series;
    private String number;
    private LocalDate issueDate;
    private LocalDateTime arrivalDate;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Purchase.PurchaseStatus status;
    private String notes;
    private List<PurchaseItemResponse> items;
}
