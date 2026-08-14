package com.sergiocodev.app.dto.customer;

import java.math.BigDecimal;

public record CustomerSummaryResponse(
    String label,
    String value,
    String prefix,
    String suffix,
    String trendValue,
    String trendDirection,
    String trendText
) {}
