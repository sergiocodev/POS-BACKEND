package com.sergiocodev.app.dto.accountpayable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountPayablePaymentResponse(
        Long id,
        Long accountPayableId,
        Long userId,
        String username,
        BigDecimal amount,
        String paymentMethod,
        String reference,
        String notes,
        LocalDateTime paymentDate) {
}
