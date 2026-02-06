package com.sergiocodev.app.dto.dashboard;

import java.math.BigDecimal;

public record EmployeePerformanceDashboard(
        String fullName,
        BigDecimal totalSold) {
}
