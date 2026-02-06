package com.sergiocodev.app.dto.cashsession;

import com.sergiocodev.app.model.CashSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashSessionResponse(
        Long id,
        String cashRegisterName,
        String username,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        BigDecimal calculatedBalance,
        BigDecimal diffAmount,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        String notes,
        CashSession.SessionStatus status,
        Long establishmentId) {
    public CashSessionResponse(CashSession session) {
        this(
                session.getId(),
                session.getCashRegister().getName(),
                session.getUser().getUsername(),
                session.getOpeningBalance(),
                session.getClosingBalance(),
                session.getCalculatedBalance(),
                session.getDiffAmount(),
                session.getOpenedAt(),
                session.getClosedAt(),
                session.getNotes(),
                session.getStatus(),
                session.getCashRegister().getEstablishment().getId());
    }
}
