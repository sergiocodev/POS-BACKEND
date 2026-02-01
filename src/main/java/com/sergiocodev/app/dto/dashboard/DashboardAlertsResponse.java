package com.sergiocodev.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAlertsResponse {
    private long expiringSoon; // Lots expiring in < 3 months
    private long lowStock; // Products below minimum stock
    private long pendingSunat; // Documents pending/rejected for > 24h
}
