package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.model.CashConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
    List<CashMovement> findByCashSessionId(Long cashSessionId);

    List<CashMovement> findByCashSessionIdAndCashConceptType(Long cashSessionId, CashConcept.ConceptType type);

    java.util.Optional<CashMovement> findByCashSessionIdAndAmountAndReference(Long cashSessionId, java.math.BigDecimal amount, String reference);
}
