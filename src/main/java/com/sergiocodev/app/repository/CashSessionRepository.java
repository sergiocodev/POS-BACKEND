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
}
