package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.model.CashConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para registrar movimientos manuales o automáticos de efectivo en caja.
 */
@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
    /**
     * Obtiene todos los movimientos realizados en una sesión de caja.
     * @param cashSessionId ID de la sesión.
     * @return Lista de movimientos.
     */
    List<CashMovement> findByCashSessionId(Long cashSessionId);

    /**
     * Filtra movimientos de una sesión por el tipo de concepto (Ingreso/Egreso).
     * @param cashSessionId ID de la sesión.
     * @param type Tipo de concepto.
     * @return Lista de movimientos filtrados.
     */
    List<CashMovement> findByCashSessionIdAndCashConceptType(Long cashSessionId, CashConcept.ConceptType type);

    /**
     * Busca un movimiento específico por sesión, monto y referencia.
     * Utilizado para evitar duplicidades en operaciones automatizadas.
     */
    java.util.Optional<CashMovement> findByCashSessionIdAndAmountAndReference(Long cashSessionId, java.math.BigDecimal amount, String reference);
}
