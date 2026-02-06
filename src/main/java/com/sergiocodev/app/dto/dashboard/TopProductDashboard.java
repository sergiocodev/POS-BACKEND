package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;

public record TopProductDashboard(
        Long id,
        String name,
        BigDecimal quantity) {
}
