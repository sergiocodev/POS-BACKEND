package com.sergiocodev.app.dto.accountpayable;

public record AccountPayableDashboardResponse(
    String label,
    String value,
    String prefix,
    String suffix,
    String trendValue,
    String trendDirection,
    String trendText
) {}
