package com.sergiocodev.app.dto.sunat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VoidInvoiceRequest(
    @NotNull(message = "El ID de la venta es obligatorio")
    Long saleId,

    @NotBlank(message = "El motivo de la anulación es obligatorio")
    String reason
) {}
