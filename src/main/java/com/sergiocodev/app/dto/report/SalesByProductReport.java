package com.sergiocodev.app.dto.report;

import java.math.BigDecimal;

public record SalesByProductReport(
    Long productId,
    String productName,
    String categoryName,
    String laboratoryName,
    String therapeuticAction,
    Long quantitySold,
    BigDecimal totalRevenue
) {}
