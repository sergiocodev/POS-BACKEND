package com.sergiocodev.app.dto.customer;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO con estadísticas generales de clientes para el dashboard.
 */
public record CustomerDashboardResponse(
        // ── KPI Cards ─────────────────────────────────────────────
        long totalCustomers,
        long activeCustomers,
        BigDecimal totalSalesAmount,
        BigDecimal averageTicket,
        double averageFrequency,

        // ── Tendencias vs mes anterior (%) ────────────────────────
        double totalCustomersTrend,
        double activeCustomersTrend,
        double salesAmountTrend,
        double averageTicketTrend,
        double frequencyTrend,

        // ── Clientes recientes ─────────────────────────────────────
        List<RecentCustomerItem> recentCustomers,

        // ── Segmentación ──────────────────────────────────────────
        long frequentCount,
        long occasionalCount,
        long newCount,
        long inactiveCount,
        long vipCount,

        // ── Top ventas por cliente ─────────────────────────────────
        List<TopCustomerItem> topCustomers,

        // ── Actividad mensual (nuevos vs activos) ──────────────────
        List<ActivityPoint> activitySeries,

        // ── Clientes por rango de edad (por puntos acumulados como proxy) ──
        List<AgeRangeItem> ageRanges
) {

    public record RecentCustomerItem(
            long id,
            String name,
            String initials,
            String avatarColor,
            String contact,
            String email,
            String lastPurchaseDate,
            BigDecimal totalPurchases,
            String status,
            String documentNumber
    ) {}

    public record TopCustomerItem(
            long id,
            String name,
            BigDecimal totalAmount,
            int salesCount,
            double percentage
    ) {}

    public record ActivityPoint(
            String date,
            long newCustomers,
            long activeCustomers
    ) {}

    public record AgeRangeItem(
            String range,
            long count
    ) {}
}
