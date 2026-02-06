package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record EmployeeSalesReport(
        Long userId,
        String userName,
        BigDecimal totalRevenue,
        BigDecimal quantitySold,
        Long transactionCount) {
}
