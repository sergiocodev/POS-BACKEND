package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByLotProductIdOrderByCreatedAtDesc(Long productId);

    List<StockMovement> findByEstablishmentIdOrderByCreatedAtDesc(Long establishmentId);
}
