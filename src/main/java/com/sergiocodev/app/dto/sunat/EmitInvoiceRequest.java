package com.sergiocodev.app.dto.sunat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmitInvoiceRequest {
    @NotNull(message = "El ID de la venta es obligatorio")
    private Long saleId;
}
