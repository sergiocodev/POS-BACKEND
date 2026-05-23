package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.SaleItem;
import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.ProductSearchResponse;
import com.sergiocodev.app.dto.sale.BarcodeScanResponse;
import java.math.BigDecimal;
import java.util.List;

public interface SaleInventoryService {
    void validateStock(Long establishmentId, Long lotId, BigDecimal quantity);
    void updateInventory(Sale sale, SaleItem item, BigDecimal baseQuantity);
    void reverseInventory(Sale sale, SaleItem item, String reason, Long userId);
    List<ProductForSaleResponse> listProductsForSale(Long establishmentId);
    List<ProductSearchResponse> searchProductsForPOS(String query, Long establishmentId);
    BarcodeScanResponse getProductByBarcode(String barcode, Long establishmentId);
}
