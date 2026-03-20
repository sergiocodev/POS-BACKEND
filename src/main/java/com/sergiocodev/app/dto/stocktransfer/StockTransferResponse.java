package com.sergiocodev.app.dto.stocktransfer;

import com.sergiocodev.app.model.StockTransfer;
import java.time.LocalDateTime;
import java.util.List;

public record StockTransferResponse(
        Long id,
        String transferNumber,
        Long sourceEstablishmentId,
        String sourceEstablishmentName,
        Long targetEstablishmentId,
        String targetEstablishmentName,
        StockTransfer.TransferStatus status,
        Long requestedById,
        String requestedByUsername,
        Long dispatchedById,
        String dispatchedByUsername,
        Long receivedById,
        String receivedByUsername,
        LocalDateTime transferDate,
        LocalDateTime dispatchDate,
        LocalDateTime receiveDate,
        String notes,
        List<StockTransferItemResponse> items) {
}
