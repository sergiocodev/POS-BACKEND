package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesChartResponse(
        LocalDate date,
        BigDecimal total) {
}
