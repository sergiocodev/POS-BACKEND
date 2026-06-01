package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.report.*;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseReportService {
    List<PurchaseReport> getPurchases(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<PurchaseDebtReport> getPurchaseDebtStatus(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<PurchasesByCategoryDetailReport> getPurchasesByCategoryDetail(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> categoryIds);

    List<PurchasesBySupplierReport> getPurchasesBySupplier(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> supplierIds);

    List<AccountsPayableSupplierReport> getAccountsPayableBySupplier(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> supplierIds);

    List<ProductPriceHistoryReport> getProductPriceHistory(LocalDateTime start, LocalDateTime end,
            Long establishmentId, Long productId);

    List<PurchasesByBuyerReport> getPurchasesByBuyer(LocalDateTime start, LocalDateTime end,
            Long establishmentId, List<Long> buyerIds);
}
