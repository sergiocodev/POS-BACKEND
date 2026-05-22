package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.DashboardService;

import com.sergiocodev.app.dto.dashboard.*;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final SaleRepository saleRepository;
    private final InventoryRepository inventoryRepository;

    public DashboardServiceImpl(SaleRepository saleRepository,
                                InventoryRepository inventoryRepository) {
        this.saleRepository = saleRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // ───────────────────────────────────────────────
    // SUMMARY CARDS (KPIs)
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummaryCards(Long establishmentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startToday = now.toLocalDate().atStartOfDay();

        LocalDateTime startYesterday = startToday.minusDays(1);
        LocalDateTime endYesterday = now.minusDays(1);

        long countToday = saleRepository.countByEstablishmentAndDateBetween(establishmentId, startToday, now);
        BigDecimal totalToday = saleRepository.sumTotalByEstablishmentAndDateBetween(establishmentId, startToday, now);
        if (totalToday == null) totalToday = BigDecimal.ZERO;

        long countYesterday = saleRepository.countByEstablishmentAndDateBetween(establishmentId, startYesterday, endYesterday);
        BigDecimal totalYesterday = saleRepository.sumTotalByEstablishmentAndDateBetween(establishmentId, startYesterday, endYesterday);
        if (totalYesterday == null) totalYesterday = BigDecimal.ZERO;

        String salesTrend = calculateTrend(totalToday, totalYesterday);
        String countTrend = calculateTrend(new BigDecimal(countToday), new BigDecimal(countYesterday));

        LocalDateTime yesterdayLimit = now.minusDays(1);

        long pendingSunat = saleRepository.countPendingSunat(
                establishmentId,
                List.of(Sale.SunatStatus.PENDING, Sale.SunatStatus.REJECTED),
                yesterdayLimit);

        long expired = inventoryRepository.countExpiredLots(establishmentId, now.toLocalDate());
        long expiringSoon = inventoryRepository.countExpiringLots(establishmentId, now.toLocalDate(), now.plusMonths(3).toLocalDate());
        long outOfStock = inventoryRepository.countOutOfStock(establishmentId);
        long totalProducts = inventoryRepository.countDistinctProductsInStock(establishmentId);

        DashboardSummaryResponse.ValueTrend salesVT = new DashboardSummaryResponse.ValueTrend(totalToday, "PEN", salesTrend);
        DashboardSummaryResponse.ValueTrendLong countVT = new DashboardSummaryResponse.ValueTrendLong(countToday, countTrend);
        DashboardSummaryResponse.StockAlertsData stockVT = new DashboardSummaryResponse.StockAlertsData(expired, expiringSoon, outOfStock);

        DashboardSummaryResponse.SummaryData data = new DashboardSummaryResponse.SummaryData(salesVT, countVT, pendingSunat, stockVT, totalProducts);

        return new DashboardSummaryResponse("today", data);
    }

    private String calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        BigDecimal diff = current.subtract(previous);
        BigDecimal trend = diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        String sign = trend.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + trend.setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    // ───────────────────────────────────────────────
    // SALES CHART (line chart)
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SalesChartResponse> getSalesChart(String range, Long establishmentId) {
        int days = "30days".equalsIgnoreCase(range) ? 30 : 7;
        LocalDate now = LocalDate.now();

        List<SalesChartResponse> result = new ArrayList<>();
        LocalDateTime rangeStart = now.minusDays(days - 1).atStartOfDay();

        Page<Sale> salesPage = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                establishmentId, rangeStart, now.atTime(LocalTime.MAX), Pageable.unpaged());
        List<Sale> salesInRange = salesPage.getContent();

        for (int i = 0; i < days; i++) {
            LocalDate date = now.minusDays(days - 1 - i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            BigDecimal dayTotal = salesInRange.stream()
                    .filter(s -> s.getDate().isAfter(dayStart) && s.getDate().isBefore(dayEnd))
                    .map(Sale::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new SalesChartResponse(date, dayTotal));
        }

        return result;
    }

    // ───────────────────────────────────────────────
    // ALERTS (detailed stock + sunat)
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public DashboardAlertsResponse getAlerts(Long establishmentId) {
        LocalDate now = LocalDate.now();
        LocalDate threeMonthsFromNow = now.plusMonths(3);
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        List<DashboardAlertsResponse.StockAlert> stockAlerts = new ArrayList<>();

        List<Inventory> expiredInventory = inventoryRepository.findExpiredLots(establishmentId, now);
        expiredInventory.forEach(inv -> stockAlerts.add(new DashboardAlertsResponse.StockAlert(
                inv.getLot().getProduct().getTradeName(),
                inv.getLot().getLotCode(),
                inv.getLot().getExpiryDate().toString(),
                inv.getQuantity().intValue(),
                "EXPIRED")));

        List<Inventory> expiringInventory = inventoryRepository.findExpiringLots(establishmentId, now, threeMonthsFromNow);
        expiringInventory.forEach(inv -> stockAlerts.add(new DashboardAlertsResponse.StockAlert(
                inv.getLot().getProduct().getTradeName(),
                inv.getLot().getLotCode(),
                inv.getLot().getExpiryDate().toString(),
                inv.getQuantity().intValue(),
                "EXPIRING_SOON")));

        List<Inventory> outOfStockInventory = inventoryRepository.findOutOfStock(establishmentId);
        outOfStockInventory.forEach(inv -> stockAlerts.add(new DashboardAlertsResponse.StockAlert(
                inv.getLot().getProduct().getTradeName(),
                inv.getLot().getLotCode(),
                inv.getLot().getExpiryDate() != null ? inv.getLot().getExpiryDate().toString() : null,
                0,
                "OUT_OF_STOCK")));

        List<Sale> sunatAlertSales = saleRepository.findSunatAlerts(
                establishmentId,
                List.of(Sale.SunatStatus.PENDING, Sale.SunatStatus.REJECTED),
                yesterday);

        List<DashboardAlertsResponse.SunatAlert> sunatAlerts = sunatAlertSales.stream()
                .map(s -> new DashboardAlertsResponse.SunatAlert(
                        s.getId(),
                        s.getDocumentType().name(),
                        s.getSeries(),
                        s.getNumber(),
                        s.getSunatStatus().name(),
                        s.getSunatMessage() != null ? s.getSunatMessage() : "Pendiente de env\u00edo"))
                .collect(Collectors.toList());

        return new DashboardAlertsResponse(stockAlerts, sunatAlerts);
    }

    // ───────────────────────────────────────────────
    // PAYMENT METHODS (distribution)
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodDistribution> getPaymentMethods(LocalDate date, Long establishmentId) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findForPaymentAnalysis(establishmentId, start, end);

        List<SalePayment> payments = sales.stream()
                .flatMap(s -> s.getPayments().stream())
                .collect(Collectors.toList());

        BigDecimal grandTotal = payments.stream()
                .map(SalePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return payments.stream()
                .collect(Collectors.groupingBy(SalePayment::getPaymentMethod))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal amount = e.getValue().stream()
                            .map(SalePayment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long count = e.getValue().size();
                    double percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? amount.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .doubleValue()
                            : 0.0;
                    return new PaymentMethodDistribution(
                            e.getKey().name(), amount, count, percentage);
                })
                .sorted((a, b) -> Double.compare(b.percentage(), a.percentage()))
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────────
    // TOP PRODUCTS
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<TopProductDashboard> getTopProducts(int limit, Long establishmentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startCurrentMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endCurrentMonth = now;

        LocalDateTime startPrevMonth = now.minusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endPrevMonth = now.minusMonths(1);

        List<Sale> currentMonthSales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                        establishmentId, startCurrentMonth, endCurrentMonth, Pageable.unpaged())
                .getContent().stream()
                .collect(Collectors.toList());

        List<Sale> prevMonthSales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                        establishmentId, startPrevMonth, endPrevMonth, Pageable.unpaged())
                .getContent().stream()
                .collect(Collectors.toList());

        Map<Long, BigDecimal> prevMonthQtyMap = prevMonthSales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId(),
                        Collectors.reducing(BigDecimal.ZERO, SaleItem::getQuantity, BigDecimal::add)));

        return currentMonthSales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(SaleItem::getProduct))
                .entrySet().stream()
                .map(e -> {
                    Product p = e.getKey();
                    BigDecimal currentQty = e.getValue().stream()
                            .map(SaleItem::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal currentAmt = e.getValue().stream()
                            .map(SaleItem::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal prevQty = prevMonthQtyMap.getOrDefault(p.getId(), BigDecimal.ZERO);
                    String trend = calculateTrend(currentQty, prevQty);

                    String categoryName = p.getCategory() != null ? p.getCategory().getName() : "Sin categor\u00eda";

                    return new TopProductDashboard(
                            p.getId(),
                            p.getTradeName(),
                            categoryName,
                            currentQty,
                            currentAmt,
                            trend);
                })
                .sorted((a, b) -> b.quantitySold().compareTo(a.quantitySold()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────────
    // EMPLOYEE PERFORMANCE
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<EmployeePerformanceDashboard> getEmployeePerformance(LocalDate date, Long establishmentId) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeForEmployee(establishmentId, start, end);

        return sales.stream()
                .collect(Collectors.groupingBy(Sale::getUser))
                .entrySet().stream()
                .filter(e -> e.getKey() != null)
                .map(e -> {
                    User user = e.getKey();
                    BigDecimal totalAmount = e.getValue().stream()
                            .map(Sale::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long salesCount = e.getValue().size();
                    return new EmployeePerformanceDashboard(
                            user.getId(),
                            user.getUsername(),
                            user.getFullName(),
                            salesCount,
                            totalAmount);
                })
                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────────
    // SALES BY CATEGORY (donut chart)
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SalesByCategoryResponse> getSalesByCategory(String range, Long establishmentId) {
        int days = "30days".equalsIgnoreCase(range) ? 30 : 7;
        LocalDate now = LocalDate.now();
        LocalDateTime rangeStart = now.minusDays(days - 1).atStartOfDay();
        LocalDateTime rangeEnd = now.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findForCategoryAnalysis(establishmentId, rangeStart, rangeEnd);

        List<SaleItem> items = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.toList());

        BigDecimal grandTotal = items.stream()
                .map(SaleItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Category, BigDecimal> byCategory = items.stream()
                .collect(Collectors.groupingBy(
                        item -> {
                            Category cat = item.getProduct().getCategory();
                            return cat != null ? cat : createUncategorized();
                        },
                        Collectors.reducing(BigDecimal.ZERO, SaleItem::getAmount, BigDecimal::add)));

        return byCategory.entrySet().stream()
                .map(e -> {
                    double pct = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? e.getValue().divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .doubleValue()
                            : 0.0;
                    return new SalesByCategoryResponse(
                            e.getKey().getId(),
                            e.getKey().getName(),
                            e.getValue(),
                            pct);
                })
                .sorted((a, b) -> Double.compare(b.percentage(), a.percentage()))
                .collect(Collectors.toList());
    }

    private Category createUncategorized() {
        Category c = new Category();
        c.setId(0L);
        c.setName("Sin categor\u00eda");
        return c;
    }

    // ───────────────────────────────────────────────
    // RECENT SALES
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<RecentSaleResponse> getRecentSales(int limit, Long establishmentId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        Page<Sale> salesPage = saleRepository.findRecentSales(establishmentId, start, end, Pageable.ofSize(limit));

        return salesPage.getContent().stream()
                .map(s -> {
                    String customerName = "Cliente General";
                    String initials = "CG";
                    if (s.getCustomer() != null && s.getCustomer().getName() != null
                            && !s.getCustomer().getName().isBlank()) {
                        customerName = s.getCustomer().getName();
                        initials = buildInitials(customerName);
                    }
                    int productCount = s.getItems() != null ? s.getItems().size() : 0;
                    return new RecentSaleResponse(
                            s.getId(),
                            customerName,
                            initials,
                            s.getDocumentType().name(),
                            productCount,
                            s.getDate(),
                            s.getTotal());
                })
                .collect(Collectors.toList());
    }

    private String buildInitials(String name) {
        if (name == null || name.isBlank()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return (parts[0].length() >= 2 ? parts[0].substring(0, 2) : parts[0]).toUpperCase();
    }

    // ───────────────────────────────────────────────
    // EXPIRING LOTS
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ExpiringLotResponse> getExpiringLots(int days, Long establishmentId) {
        LocalDate now = LocalDate.now();
        LocalDate limit = now.plusDays(days);

        List<Inventory> expiringInventory = inventoryRepository.findExpiringLots(establishmentId, now, limit);

        return expiringInventory.stream()
                .sorted(Comparator.comparing(inv -> inv.getLot().getExpiryDate()))
                .map(inv -> {
                    long daysUntil = ChronoUnit.DAYS.between(now, inv.getLot().getExpiryDate());
                    return new ExpiringLotResponse(
                            inv.getId(),
                            inv.getLot().getProduct().getTradeName(),
                            inv.getLot().getLotCode(),
                            inv.getQuantity().intValue(),
                            inv.getLot().getExpiryDate(),
                            daysUntil,
                            daysUntil <= 7);
                })
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────────
    // LOW STOCK ITEMS
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<LowStockItemResponse> getLowStockItems(int limit, Long establishmentId) {
        Page<Inventory> lowStockPage = inventoryRepository.findLowStockItems(establishmentId, Pageable.ofSize(limit));

        return lowStockPage.getContent().stream()
                .map(inv -> {
                    int current = inv.getQuantity().intValue();
                    int min = inv.getMinStock();
                    double level = min > 0 ? (double) current / min : 0.0;
                    String categoryName = "Sin categor\u00eda";
                    Product product = inv.getLot().getProduct();
                    if (product.getCategory() != null) {
                        categoryName = product.getCategory().getName();
                    }
                    return new LowStockItemResponse(
                            product.getId(),
                            product.getTradeName(),
                            categoryName,
                            current,
                            min,
                            Math.round(level * 100.0) / 100.0,
                            level < 0.2);
                })
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────────
    // FULL DASHBOARD (unified endpoint)
    // ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public FullDashboardResponse getFullDashboard(Long establishmentId) {
        LocalDate today = LocalDate.now();

        return new FullDashboardResponse(
                getSummaryCards(establishmentId),
                getSalesChart("7days", establishmentId),
                getSalesByCategory("7days", establishmentId),
                getPaymentMethods(today, establishmentId),
                getTopProducts(5, establishmentId),
                getEmployeePerformance(today, establishmentId),
                getRecentSales(10, establishmentId),
                getLowStockItems(10, establishmentId),
                getExpiringLots(30, establishmentId));
    }
}
