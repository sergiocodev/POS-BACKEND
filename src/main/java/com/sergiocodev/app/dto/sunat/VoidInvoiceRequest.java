package com.sergiocodev.app.dto.sunat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoidInvoiceRequest {
    @NotNull(message = "El ID de la venta es obligatorio")
    private Long saleId;

    @NotBlank(message = "El motivo de la anulación es obligatorio")
    private String reason;
}
