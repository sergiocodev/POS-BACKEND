package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.Purchase;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import com.sergiocodev.app.model.AccountPayablePayment;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    @NotNull(message = "Document type is required")
    private Purchase.PurchaseDocumentType documentType;

    @Size(max = 20, message = "Series cannot exceed 20 characters")
    private String series;

    @Size(max = 20, message = "Number cannot exceed 20 characters")
    private String number;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    private String notes;

    @NotNull(message = "Items are required")
    private List<PurchaseItemRequest> items;

    @NotNull(message = "Payment condition is required")
    private PaymentCondition paymentCondition;

    private BigDecimal initialPayment;

    private AccountPayablePayment.PaymentMethod paymentMethod;

    private LocalDate dueDate;

    public enum PaymentCondition {
        CASH, CREDIT
    }
}
