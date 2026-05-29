package com.sergiocodev.app.dto.cashsession;

public record CashSessionSummaryResponse(
    String label,
    String value,
    String prefix,
    String suffix,
    String trendValue,
    String trendDirection,
    String trendText
) {}
