package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.stockmovement.StockMovementRequest;
import com.sergiocodev.app.dto.stockmovement.StockMovementResponse;
import java.util.List;

public interface StockMovementService {
    StockMovementResponse create(StockMovementRequest request);

    List<StockMovementResponse> getAll();

    List<StockMovementResponse> getByProduct(Long productId);

    List<StockMovementResponse> getByEstablishment(Long establishmentId);
}
