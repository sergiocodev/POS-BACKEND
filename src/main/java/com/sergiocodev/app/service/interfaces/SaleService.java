package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.sale.BarcodeScanResponse;
import com.sergiocodev.app.dto.sale.CartCalculationRequest;
import com.sergiocodev.app.dto.sale.CartCalculationResponse;
import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.ProductSearchResponse;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import com.sergiocodev.app.dto.sunat.EmitInvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SaleService {
        SaleResponse create(SaleRequest request, Long userId);

        List<SaleResponse> getAll(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

        /** Paginated version of getAll for large datasets */
        Page<SaleResponse> getAllPaged(
                        java.time.LocalDateTime startDate,
                        java.time.LocalDateTime endDate,
                        String documentType,
                        String series,
                        String number,
                        String customerName,
                        String customerDocument,
                        String vendedorName,
                        String status,
                        String sunatStatus,
                        String total,
                        String paymentMethod,
                        String columnDate,
                        Pageable pageable);

        SaleResponse getById(Long id);

        void cancel(Long id, Long userId);

        byte[] getPdf(Long id);

        String getXml(Long id);

        String getCdr(Long id);

        SaleResponse createCreditNote(Long id, String reason, Long userId);

        SaleResponse createDebitNote(Long id, String reason, Long userId);

        void invalidate(Long id, String reason, Long userId);

        List<ProductForSaleResponse> listProductsForSale(Long establishmentId);

        List<ProductSearchResponse> searchProductsForPOS(String query, Long establishmentId);

        BarcodeScanResponse getProductByBarcode(String barcode, Long establishmentId);

        CartCalculationResponse calculateCartTotals(CartCalculationRequest request);

        SaleResponse processSaleTransaction(SaleRequest request, Long userId);

        byte[] getSaleDocumentPDF(Long id, String format);

        EmitInvoiceResponse emitInvoiceToOSE(Long saleId);

        com.sergiocodev.app.dto.sale.SaleSummaryResponse getSummary(
                        java.time.LocalDateTime startDate,
                        java.time.LocalDateTime endDate,
                        String documentType,
                        String series,
                        String number,
                        String customerName,
                        String customerDocument,
                        String vendedorName,
                        String status,
                        String sunatStatus,
                        String total,
                        String paymentMethod,
                        String columnDate);
}
