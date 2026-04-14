package com.sergiocodev.app.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for stock movement events and logs them for audit purposes.
 * Can be extended to send notifications, update external systems, etc.
 */
@Component
public class StockEventListener {

    private static final Logger log = LoggerFactory.getLogger("com.sergiocodev.app.stock.audit");

    @EventListener
    @Async
    public void onStockMovement(StockMovementEvent event) {
        log.info("STOCK_MOVEMENT: type={} establishment={} product={} lot={} quantity={} balanceAfter={} reason={} ref={}/{} userId={}",
                event.getMovementType(),
                event.getEstablishmentId(),
                event.getProductId(),
                event.getLotId(),
                event.getQuantity(),
                event.getBalanceAfter(),
                event.getReason(),
                event.getReferenceTable(),
                event.getReferenceId(),
                event.getUserId());
    }

    @EventListener
    @Async
    public void onLowStock(StockMovementEvent event) {
        if (event.getBalanceAfter().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            log.warn("STOCK_OUT: Product {} at establishment {} is out of stock",
                    event.getProductId(), event.getEstablishmentId());
        }
    }
}
