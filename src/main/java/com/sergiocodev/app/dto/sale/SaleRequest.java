package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.Sale;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    private Long customerId;

    @NotNull(message = "Document type is required")
    private Sale.SaleDocumentType documentType;

    @NotNull(message = "Items are required")
    private List<SaleItemRequest> items;

    @NotNull(message = "Payments are required")
    private List<SalePaymentRequest> payments;
}
