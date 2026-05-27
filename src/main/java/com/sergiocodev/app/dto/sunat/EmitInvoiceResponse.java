package com.sergiocodev.app.dto.sunat;

import lombok.Builder;

@Builder
public record EmitInvoiceResponse(
    Long saleId,
    String sunatStatus,
    String sunatMessage,
    String xmlUrl,
    String cdrUrl,
    String hashCpe
) {}
