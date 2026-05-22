package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.stockmovement.StockMovementRequest;
import com.sergiocodev.app.dto.stockmovement.StockMovementResponse;
import com.sergiocodev.app.model.Establishment;
import com.sergiocodev.app.model.ProductLot;
import com.sergiocodev.app.model.StockMovement;
import com.sergiocodev.app.model.User;
import java.math.BigDecimal;
import java.util.List;

public interface StockMovementService {
    StockMovementResponse create(StockMovementRequest request);

    List<StockMovementResponse> getAll();

    List<StockMovementResponse> getByProduct(Long productId);

    List<StockMovementResponse> getByEstablishment(Long establishmentId);

    /**
     * Record a stock movement for a sale.
     */
    StockMovement recordSaleMovement(Establishment establishment, ProductLot lot,
                                     BigDecimal quantity, BigDecimal balanceAfter,
                                     Long saleId, User user);

    /**
     * Record a stock movement for a purchase (stock entry).
     */
    StockMovement recordPurchaseMovement(Establishment establishment, ProductLot lot,
                                         BigDecimal quantity, BigDecimal balanceAfter,
                                         Long purchaseId, User user);

    /**
     * Record a stock adjustment movement (theft, breakage, counting error, etc.).
     */
    StockMovement recordAdjustmentMovement(Establishment establishment, ProductLot lot,
                                           BigDecimal quantityChange, BigDecimal balanceAfter,
                                           String reason, User user);

    /**
     * Record a stock transfer movement (outgoing or incoming).
     */
    StockMovement recordTransferMovement(Establishment establishment, ProductLot lot,
                                         BigDecimal quantity, BigDecimal balanceAfter,
                                         Long transferId, User user, String referenceTable);

    /**
     * Record a stock reversal (e.g., cancelled sale returns stock).
     */
    StockMovement recordReversalMovement(Establishment establishment, ProductLot lot,
                                         BigDecimal quantity, BigDecimal balanceAfter,
                                         String reason, Long originalReferenceId, User user);
}
