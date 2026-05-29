package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashSessionReport(
        Long sessionId,
        String cashRegisterName,
        String username,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        BigDecimal calculatedBalance,
        BigDecimal diffAmount,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        String status
) {}
