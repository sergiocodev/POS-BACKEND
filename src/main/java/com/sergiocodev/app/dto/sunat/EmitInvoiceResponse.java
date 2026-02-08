package com.sergiocodev.app.dto.sunat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmitInvoiceResponse {
    private Long saleId;
    private String sunatStatus;
    private String sunatMessage;
    private String xmlUrl;
    private String cdrUrl;
    private String hashCpe;
}
