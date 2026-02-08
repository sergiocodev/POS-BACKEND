package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
    List<CashMovement> findByCashSessionId(Long cashSessionId);

    List<CashMovement> findByCashSessionIdAndType(Long cashSessionId,
            com.sergiocodev.app.model.CashMovement.MovementType type);
}
