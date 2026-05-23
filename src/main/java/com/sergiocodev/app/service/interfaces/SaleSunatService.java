package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.sunat.EmitInvoiceResponse;

public interface SaleSunatService {
    EmitInvoiceResponse emitInvoiceToOSE(Long saleId);
    String getXml(Long saleId);
    String getCdr(Long saleId);
}
