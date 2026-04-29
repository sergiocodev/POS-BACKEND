package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para los ítems o líneas de detalle de una venta registrada.
 */
@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    /**
     * Recupera el detalle de los productos dispensados en una venta.
     */
    List<SaleItem> findBySaleId(Long saleId);
}
