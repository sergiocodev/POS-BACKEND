package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record SalesByLaboratoryReport(
        Long laboratoryId,
        String laboratoryName,
        BigDecimal totalRevenue,
        BigDecimal quantitySold,
        Long productCount) {
}
