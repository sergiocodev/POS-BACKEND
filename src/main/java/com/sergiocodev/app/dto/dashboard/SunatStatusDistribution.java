package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;

public record SunatStatusDistribution(
        String status,
        long count,
        BigDecimal amount,
        double percentage) {
}
