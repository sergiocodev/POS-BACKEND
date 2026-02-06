package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record HourlyHeatReport(
        int hour,
        BigDecimal totalRevenue,
        Long transactionCount) {
}
