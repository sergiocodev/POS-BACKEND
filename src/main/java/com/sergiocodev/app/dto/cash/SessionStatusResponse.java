package com.sergiocodev.app.dto.cash;

import com.sergiocodev.app.model.CashSession.SessionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionStatusResponse(
        Long sessionId,
        String cashRegisterName,
        LocalDateTime openedAt,
        SessionStatus status,

        // ── Saldos ──────────────────────────────────
        BigDecimal openingBalance,
        BigDecimal calculatedBalance,       // saldo teórico = lo que debería haber

        // ── Entradas de efectivo (+) ─────────────────
        BigDecimal totalCashSales,          // ventas cobradas en EFECTIVO
        BigDecimal totalArCashPayments,     // cobros de CxC en EFECTIVO
        BigDecimal totalCashInflows,        // ingresos manuales (CashMovements IN)

        // ── Salidas de efectivo (-) ──────────────────
        BigDecimal totalApCashPayments,     // pagos a proveedores en EFECTIVO
        BigDecimal totalCashOutflows,       // egresos manuales (CashMovements OUT)

        // ── Métodos digitales (solo visualización) ───
        BigDecimal totalSalesYape,
        BigDecimal totalSalesPlin,
        BigDecimal totalSalesTarjeta,
        BigDecimal totalSalesTransferencia,
        BigDecimal totalDigital            // suma de todos los digitales (sin efectivo)
) {
}
