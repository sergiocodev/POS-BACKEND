package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.stocktransfer.StockTransferRequest;
import com.sergiocodev.app.dto.stocktransfer.StockTransferResponse;

import java.util.List;

public interface StockTransferService {
    StockTransferResponse create(StockTransferRequest request, Long userId);

    List<StockTransferResponse> getBySourceEstablishmentId(Long establishmentId);

    List<StockTransferResponse> getByTargetEstablishmentId(Long establishmentId);

    StockTransferResponse getById(Long id);

    StockTransferResponse dispatchTransfer(Long id, Long userId);

    StockTransferResponse receiveTransfer(Long id, Long userId);

    StockTransferResponse cancelTransfer(Long id, Long userId);
}
