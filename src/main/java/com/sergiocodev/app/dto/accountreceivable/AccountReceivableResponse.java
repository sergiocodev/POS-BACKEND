package com.sergiocodev.app.dto.accountreceivable;

import com.sergiocodev.app.model.AccountReceivable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountReceivableResponse(
        Long id,
        Long saleId,
        String customerName,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal pendingBalance,
        AccountReceivable.ReceivableStatus status,
        LocalDate dueDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
