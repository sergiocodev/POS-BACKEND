package com.sergiocodev.app.dto.sunat;

import jakarta.validation.constraints.NotNull;

public record EmitInvoiceRequest(
    @NotNull(message = "El ID de la venta es obligatorio")
    Long saleId
) {}
