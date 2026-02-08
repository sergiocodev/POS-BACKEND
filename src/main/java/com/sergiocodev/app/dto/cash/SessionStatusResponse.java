package com.sergiocodev.app.dto.cash;

import com.sergiocodev.app.model.CashSession.SessionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionStatusResponse(
        Long sessionId,
        String cashRegisterName,
        BigDecimal openingBalance,
        BigDecimal calculatedBalance,
        BigDecimal totalSales, // Pagos en efectivo
        BigDecimal totalIncome, // Ingresos (CashMovements)
        BigDecimal totalExpenses, // Egresos (CashMovements)
        BigDecimal totalPurchases, // Compras en efectivo
        LocalDateTime openedAt,
        SessionStatus status) {
}
