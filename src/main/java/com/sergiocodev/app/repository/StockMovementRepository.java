package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByLotProductIdOrderByCreatedAtDesc(Long productId);

    List<StockMovement> findByEstablishmentIdOrderByCreatedAtDesc(Long establishmentId);

    @org.springframework.data.jpa.repository.Query("SELECT sm FROM StockMovement sm JOIN FETCH sm.lot l JOIN FETCH l.product "
            +
            "WHERE l.product.id = :productId ORDER BY sm.createdAt DESC")
    List<StockMovement> findKardexByProductId(
            @org.springframework.data.repository.query.Param("productId") Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT sm FROM StockMovement sm WHERE sm.lot.id = :lotId ORDER BY sm.createdAt DESC")
    List<StockMovement> findKardexByLotId(@org.springframework.data.repository.query.Param("lotId") Long lotId);
}
