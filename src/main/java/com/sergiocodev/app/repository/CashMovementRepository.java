package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.model.CashConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

/**
 * Repositorio para registrar movimientos manuales o automáticos de efectivo en caja.
 */
@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long>, JpaSpecificationExecutor<CashMovement> {
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

    @Query("SELECT c FROM CashMovement c WHERE c.cashSession.cashRegister.establishment.id = :establishmentId AND c.createdAt >= :startDate AND c.createdAt <= :endDate AND c.cashConcept.type = 'OUT'")
    List<CashMovement> findExpensesByEstablishmentAndDateRange(
        @Param("establishmentId") Long establishmentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
