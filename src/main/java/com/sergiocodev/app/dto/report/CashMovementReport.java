package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashMovementReport(
        Long id,
        String conceptName,
        String type,           // "INGRESO" / "EGRESO"
        BigDecimal amount,
        String reference,
        String description,
        String username,
        LocalDateTime createdAt
) {}
