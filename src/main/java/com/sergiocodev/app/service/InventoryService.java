package com.sergiocodev.app.service;

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

    List<com.sergiocodev.app.dto.inventory.LowStockAlertResponse> getLowStockAlerts();

    List<com.sergiocodev.app.dto.inventory.ExpiringLotResponse> getExpiringLots(Integer days);

    InventoryResponse registerStockAdjustment(com.sergiocodev.app.dto.inventory.StockAdjustmentRequest request);

    List<com.sergiocodev.app.dto.inventory.KardexHistoryResponse> getKardexHistoryByProduct(Long productId);

    List<com.sergiocodev.app.dto.inventory.KardexHistoryResponse> getKardexHistoryByLot(Long lotId);
}
