package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para el control de sesiones de apertura y cierre de caja.
 */
@Repository
public interface CashSessionRepository extends JpaRepository<CashSession, Long> {
        /**
         * Busca una sesión por caja, usuario y estado. 
         * Generalmente usado para validar si un usuario ya tiene una caja abierta.
         */
        Optional<CashSession> findByCashRegisterIdAndUserIdAndStatus(Long cashRegisterId, Long userId,
                        CashSession.SessionStatus status);

        /**
         * Encuentra la sesión activa de un usuario.
         */
        Optional<CashSession> findByUserIdAndStatus(Long userId, CashSession.SessionStatus status);

        /**
         * Lista sesiones de un usuario por su estado, ordenadas por apertura descendente.
         */
        java.util.List<CashSession> findByUserIdAndStatusOrderByOpenedAtDesc(Long userId,
                        CashSession.SessionStatus status);

        /**
         * Verifica si una caja registradora específica tiene alguna sesión en un estado dado.
         */
        java.util.Optional<CashSession> findByCashRegisterIdAndStatus(Long cashRegisterId,
                        CashSession.SessionStatus status);

        @org.springframework.data.jpa.repository.Query("SELECT c FROM CashSession c WHERE (:establishmentId IS NULL OR c.cashRegister.establishment.id = :establishmentId)")
        org.springframework.data.domain.Page<CashSession> findAllByEstablishmentId(
                @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId, 
                org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query(
                "SELECT c FROM CashSession c WHERE " +
                "(:establishmentId IS NULL OR c.cashRegister.establishment.id = :establishmentId) AND " +
                "c.openedAt >= :start AND c.openedAt <= :end " +
                "ORDER BY c.openedAt DESC")
        java.util.List<CashSession> findByEstablishmentAndDateRange(
                @org.springframework.data.repository.query.Param("establishmentId") Long establishmentId,
                @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}
