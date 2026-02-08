package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashSessionRepository extends JpaRepository<CashSession, Long> {
        Optional<CashSession> findByCashRegisterIdAndUserIdAndStatus(Long cashRegisterId, Long userId,
                        CashSession.SessionStatus status);

        Optional<CashSession> findByUserIdAndStatus(Long userId, CashSession.SessionStatus status);

        java.util.List<CashSession> findByUserIdAndStatusOrderByOpenedAtDesc(Long userId,
                        CashSession.SessionStatus status);

        java.util.Optional<CashSession> findByCashRegisterIdAndStatus(Long cashRegisterId,
                        CashSession.SessionStatus status);
}
