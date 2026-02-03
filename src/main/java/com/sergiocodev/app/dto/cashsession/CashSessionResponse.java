package com.sergiocodev.app.dto.cashsession;

import com.sergiocodev.app.model.CashSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashSessionResponse {

    private Long id;
    private String cashRegisterName;
    private String username;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal calculatedBalance;
    private BigDecimal diffAmount;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String notes;
    private CashSession.SessionStatus status;
    private Long establishmentId;

    public CashSessionResponse(CashSession session) {
        this.id = session.getId();
        this.cashRegisterName = session.getCashRegister().getName();
        this.establishmentId = session.getCashRegister().getEstablishment().getId();
        this.username = session.getUser().getUsername();
        this.openingBalance = session.getOpeningBalance();
        this.closingBalance = session.getClosingBalance();
        this.calculatedBalance = session.getCalculatedBalance();
        this.diffAmount = session.getDiffAmount();
        this.openedAt = session.getOpenedAt();
        this.closedAt = session.getClosedAt();
        this.notes = session.getNotes();
        this.status = session.getStatus();
    }
}
