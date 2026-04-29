package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para los ítems o renglones individuales de una factura de compra.
 */
@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    /**
     * Recupera el detalle de productos adquiridos en una compra.
     */
    List<PurchaseItem> findByPurchaseId(Long purchaseId);
}
