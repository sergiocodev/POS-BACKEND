package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountPayableDashboardResponse(
        Long accountPayableId,
        String supplierName,
        String documentNumber,
        BigDecimal pendingBalance,
        LocalDate dueDate,
        boolean isOverdue) {
}
