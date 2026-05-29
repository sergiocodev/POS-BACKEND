package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.cashmovement.CashMovementRequest;
import com.sergiocodev.app.dto.cashmovement.CashMovementResponse;
import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CashMovementService {
    Page<CashMovementResponse> findAll(String createdAt, String conceptName, String description, String type, String reference, String username, Pageable pageable);
    List<CashMovementResponse> findBySessionId(Long sessionId);
    CashMovementResponse createManualMovement(CashMovementRequest request);
    CashMovementResponse registerInternalMovement(CashSession session, User user, CashConcept concept, BigDecimal amount, String reference, String description);
    CashMovementResponse getById(Long id);
    void delete(Long id);
}
