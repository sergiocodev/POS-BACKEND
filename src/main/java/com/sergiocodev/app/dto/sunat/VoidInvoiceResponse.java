package com.sergiocodev.app.dto.sunat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoidInvoiceResponse {
    private Long voidedDocumentId;
    private Long saleId;
    private String ticketSunat;
    private String sunatStatus;
    private String message;
    private boolean stockReversed;
}
