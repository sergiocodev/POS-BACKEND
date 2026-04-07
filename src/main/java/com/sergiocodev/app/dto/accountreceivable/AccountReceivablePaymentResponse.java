package com.sergiocodev.app.dto.accountreceivable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountReceivablePaymentResponse(
        Long id,
        Long accountReceivableId,
        String customerName,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal pendingBalance,
        Long cashSessionId,
        Long userId,
        String username,
        BigDecimal amount,
        String paymentMethod,
        String reference,
        String notes,
        LocalDateTime paymentDate) {
}
