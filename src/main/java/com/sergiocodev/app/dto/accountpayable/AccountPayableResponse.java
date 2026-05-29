package com.sergiocodev.app.dto.accountpayable;

import com.sergiocodev.app.model.AccountPayable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountPayableResponse(
    Long id,
    Long purchaseId,
    String purchaseIdentifier,
    String supplierName,
    BigDecimal totalAmount,
    BigDecimal amountPaid,
    BigDecimal pendingBalance,
    AccountPayable.PayableStatus status,
    Long daysUntilDue,
    LocalDate dueDate,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
