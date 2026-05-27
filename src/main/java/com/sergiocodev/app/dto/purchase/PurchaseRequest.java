package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.Purchase;
import com.sergiocodev.app.model.AccountPayablePayment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public record PurchaseRequest(
    @NotNull(message = "Supplier ID is required")
    Long supplierId,

    @NotNull(message = "Establishment ID is required")
    Long establishmentId,

    @NotNull(message = "Document type is required")
    Purchase.PurchaseDocumentType documentType,

    @Size(max = 20, message = "Series cannot exceed 20 characters")
    String series,

    @Size(max = 20, message = "Number cannot exceed 20 characters")
    String number,

    @NotNull(message = "Issue date is required")
    LocalDate issueDate,

    String notes,

    @NotNull(message = "Items are required")
    List<PurchaseItemRequest> items,

    @NotNull(message = "Payment condition is required")
    PaymentCondition paymentCondition,

    BigDecimal initialPayment,

    AccountPayablePayment.PaymentMethod paymentMethod,

    LocalDate dueDate
) {
    public enum PaymentCondition {
        CASH, CREDIT
    }
}
