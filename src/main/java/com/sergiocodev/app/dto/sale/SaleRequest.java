package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.Sale;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SaleRequest(
        @NotNull(message = "Establishment ID is required") Long establishmentId,

        Long customerId,

        @NotNull(message = "Document type is required") Sale.SaleDocumentType documentType,

        // Campos opcionales para Notas de Crédito/Débito
        Long relatedSaleId,
        String noteCode,
        String noteReason,

        @NotNull(message = "Items are required") List<SaleItemRequest> items,

        @NotNull(message = "Payments are required") List<SalePaymentRequest> payments) {
}
