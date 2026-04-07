package com.sergiocodev.app.dto.cashmovement;

import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.model.CashMovement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashMovementResponse(
        Long id,
        Long cashSessionId,
        Long userId,
        String username,
        Long conceptId,
        String conceptName,
        BigDecimal amount,
        CashConcept.ConceptType type,
        String reference,
        String description,
        LocalDateTime createdAt
) {
    public CashMovementResponse(CashMovement entity) {
        this(
                entity.getId(),
                entity.getCashSession() != null ? entity.getCashSession().getId() : null,
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getUser() != null ? entity.getUser().getUsername() : null,
                entity.getCashConcept() != null ? entity.getCashConcept().getId() : null,
                entity.getCashConcept() != null ? entity.getCashConcept().getName() : null,
                entity.getAmount(),
                entity.getCashConcept() != null ? entity.getCashConcept().getType() : null,
                entity.getReference(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }
}
