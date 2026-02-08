package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import java.util.List;

public interface SaleService {
    SaleResponse create(SaleRequest request, Long userId);

    List<SaleResponse> getAll();

    SaleResponse getById(Long id);

    void cancel(Long id);

    byte[] getPdf(Long id);

    String getXml(Long id);

    String getCdr(Long id);

    SaleResponse createCreditNote(Long id, String reason, Long userId);

    SaleResponse createDebitNote(Long id, String reason, Long userId);

    void invalidate(Long id, String reason, Long userId);

    List<ProductForSaleResponse> listProductsForSale(Long establishmentId);

    List<com.sergiocodev.app.dto.sale.ProductSearchResponse> searchProductsForPOS(String query, Long establishmentId);

    com.sergiocodev.app.dto.sale.BarcodeScanResponse getProductByBarcode(String barcode, Long establishmentId);

    com.sergiocodev.app.dto.sale.CartCalculationResponse calculateCartTotals(
            com.sergiocodev.app.dto.sale.CartCalculationRequest request);

    SaleResponse processSaleTransaction(SaleRequest request, Long userId);

    byte[] getSaleDocumentPDF(Long id, String format);
}
