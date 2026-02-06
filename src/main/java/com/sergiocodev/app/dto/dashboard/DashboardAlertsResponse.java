package com.sergiocodev.app.dto.dashboard;

public record DashboardAlertsResponse(
        long expiringSoon, // Lots expiring in < 3 months
        long lowStock, // Products below minimum stock
        long pendingSunat // Documents pending/rejected for > 24h
) {
}
