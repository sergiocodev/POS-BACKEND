package com.sergiocodev.app.dto.accountreceivable;

public record AccountReceivableDashboardResponse(
    String label,
    String value,
    String prefix,
    String suffix,
    String trendValue,
    String trendDirection,
    String trendText
) {}
