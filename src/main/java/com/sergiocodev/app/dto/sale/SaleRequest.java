package com.sergiocodev.app.dto.sale;

import com.sergiocodev.app.model.Sale;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record SaleRequest(
                @NotNull(message = "Establishment ID is required") Long establishmentId,

                Long customerId,

                @NotNull(message = "Document type is required") Sale.SaleDocumentType documentType,

                @Size(min = 3, max = 10, message = "Series must be 3-10 characters") String series,

                // Campos opcionales para Notas de Crédito/Débito
                Long relatedSaleId,
                String noteCode,
                String noteReason,

                @NotEmpty(message = "At least one item is required") @Valid List<SaleItemRequest> items,

                @NotEmpty(message = "At least one payment is required") @Valid List<SalePaymentRequest> payments,

                Sale.PaymentCondition paymentCondition,

                @Future(message = "Due date must be in the future") LocalDate dueDate) {
}
