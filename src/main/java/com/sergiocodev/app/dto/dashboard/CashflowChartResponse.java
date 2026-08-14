package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashflowChartResponse(
        LocalDate date,
        BigDecimal income,
        BigDecimal expense) {
}
