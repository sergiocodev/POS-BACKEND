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
}
