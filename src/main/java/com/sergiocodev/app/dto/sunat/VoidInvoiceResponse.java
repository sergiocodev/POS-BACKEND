package com.sergiocodev.app.dto.sunat;

import lombok.Builder;

@Builder
public record VoidInvoiceResponse(
    Long voidedDocumentId,
    Long saleId,
    String ticketSunat,
    String sunatStatus,
    String message,
    boolean stockReversed
) {}
