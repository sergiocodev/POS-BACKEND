package com.sergiocodev.app.dto.purchase;

import com.sergiocodev.app.model.Purchase;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

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
}
