package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.inventory.ExpiringLotResponse;
import com.sergiocodev.app.dto.inventory.KardexHistoryResponse;
import com.sergiocodev.app.dto.inventory.StockAdjustmentRequest;
import com.sergiocodev.app.dto.inventory.LowStockAlertResponse;
import com.sergiocodev.app.dto.inventory.InventoryRequest;
import com.sergiocodev.app.dto.inventory.InventoryResponse;
import java.util.List;

public interface InventoryService {
    InventoryResponse updateStock(InventoryRequest request);

    List<InventoryResponse> getAll();

    List<InventoryResponse> getByEstablishment(Long establishmentId);

    InventoryResponse getById(Long id);

    List<InventoryResponse> getAlerts();

    List<InventoryResponse> getLowStock();

    List<LowStockAlertResponse> getLowStockAlerts();

    List<ExpiringLotResponse> getExpiringLots(Integer days);

    InventoryResponse registerStockAdjustment(StockAdjustmentRequest request);

    List<KardexHistoryResponse> getKardexHistoryByProduct(Long productId);

    List<KardexHistoryResponse> getKardexHistoryByLot(Long lotId);

    void reverseStockForSale(Long saleId);
}
