package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.dashboard.*;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
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
public class DashboardServiceImpl implements DashboardService {

        private final SaleRepository saleRepository;
        private final InventoryRepository inventoryRepository;

        public DashboardServiceImpl(SaleRepository saleRepository,
                        InventoryRepository inventoryRepository) {
                this.saleRepository = saleRepository;
                this.inventoryRepository = inventoryRepository;
        }

        // ───────────────────────────────────────────────
        //  SUMMARY CARDS (KPIs)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public DashboardSummaryResponse getSummaryCards(Long establishmentId) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startToday = now.toLocalDate().atStartOfDay();

                List<Sale> salesToday = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startToday)
                                                && s.getDate().isBefore(now))
                                .collect(Collectors.toList());

                LocalDateTime startYesterday = startToday.minusDays(1);
                LocalDateTime endYesterday = now.minusDays(1);
                List<Sale> salesYesterday = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startYesterday)
                                                && s.getDate().isBefore(endYesterday))
                                .collect(Collectors.toList());

                BigDecimal totalToday = salesToday.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                BigDecimal totalYesterday = salesYesterday.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                String salesTrend = calculateTrend(totalToday, totalYesterday);

                long countToday = salesToday.size();
                long countYesterday = salesYesterday.size();
                String countTrend = calculateTrend(new BigDecimal(countToday), new BigDecimal(countYesterday));

                LocalDateTime threeMonthsFromNow = now.plusMonths(3);
                LocalDateTime yesterday = now.minusDays(1);

                long pendingSunat = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && (s.getSunatStatus() == Sale.SunatStatus.PENDING
                                                                || s.getSunatStatus() == Sale.SunatStatus.REJECTED)
                                                && s.getDate().isBefore(yesterday))
                                .count();

                List<Inventory> allInventory = inventoryRepository.findAll().stream()
                                .filter(inv -> inv.getEstablishment().getId().equals(establishmentId))
                                .collect(Collectors.toList());

                long expired = allInventory.stream()
                                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                                && inv.getLot().getExpiryDate() != null
                                                && inv.getLot().getExpiryDate().isBefore(now.toLocalDate()))
                                .count();

                long expiringSoon = allInventory.stream()
                                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                                && inv.getLot().getExpiryDate() != null
                                                && !inv.getLot().getExpiryDate().isBefore(now.toLocalDate())
                                                && inv.getLot().getExpiryDate()
                                                                .isBefore(threeMonthsFromNow.toLocalDate()))
                                .count();

                long outOfStock = allInventory.stream()
                                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
                                .count();

                DashboardSummaryResponse.ValueTrend salesVT = new DashboardSummaryResponse.ValueTrend(totalToday, "PEN",
                                salesTrend);
                DashboardSummaryResponse.ValueTrendLong countVT = new DashboardSummaryResponse.ValueTrendLong(
                                countToday, countTrend);
                DashboardSummaryResponse.StockAlertsData stockVT = new DashboardSummaryResponse.StockAlertsData(expired,
                                expiringSoon, outOfStock);

                DashboardSummaryResponse.SummaryData data = new DashboardSummaryResponse.SummaryData(salesVT, countVT,
                                pendingSunat, stockVT);

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
        //  SALES CHART (line chart)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<SalesChartResponse> getSalesChart(String range, Long establishmentId) {
                int days = "30days".equalsIgnoreCase(range) ? 30 : 7;
                LocalDate now = LocalDate.now();

                List<SalesChartResponse> result = new ArrayList<>();
                LocalDateTime rangeStart = now.minusDays(days - 1).atStartOfDay();

                List<Sale> salesInRange = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(rangeStart))
                                .collect(Collectors.toList());

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
        //  ALERTS (detailed stock + sunat)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public DashboardAlertsResponse getAlerts(Long establishmentId) {
                LocalDate now = LocalDate.now();
                LocalDate threeMonthsFromNow = now.plusMonths(3);
                LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

                List<Inventory> allInventory = inventoryRepository.findAll().stream()
                                .filter(inv -> inv.getEstablishment().getId().equals(establishmentId))
                                .collect(Collectors.toList());

                // Stock alerts: expired, expiring soon, out of stock
                List<DashboardAlertsResponse.StockAlert> stockAlerts = new ArrayList<>();

                allInventory.stream()
                                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                                && inv.getLot().getExpiryDate() != null
                                                && inv.getLot().getExpiryDate().isBefore(now))
                                .forEach(inv -> stockAlerts.add(new DashboardAlertsResponse.StockAlert(
                                                inv.getLot().getProduct().getTradeName(),
                                                inv.getLot().getLotCode(),
                                                inv.getLot().getExpiryDate().toString(),
                                                inv.getQuantity().intValue(),
                                                "EXPIRED")));

                allInventory.stream()
                                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                                && inv.getLot().getExpiryDate() != null
                                                && !inv.getLot().getExpiryDate().isBefore(now)
                                                && inv.getLot().getExpiryDate().isBefore(threeMonthsFromNow))
                                .forEach(inv -> stockAlerts.add(new DashboardAlertsResponse.StockAlert(
                                                inv.getLot().getProduct().getTradeName(),
                                                inv.getLot().getLotCode(),
                                                inv.getLot().getExpiryDate().toString(),
                                                inv.getQuantity().intValue(),
                                                "EXPIRING_SOON")));

                allInventory.stream()
                                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
                                .forEach(inv -> stockAlerts.add(new DashboardAlertsResponse.StockAlert(
                                                inv.getLot().getProduct().getTradeName(),
                                                inv.getLot().getLotCode(),
                                                inv.getLot().getExpiryDate() != null
                                                                ? inv.getLot().getExpiryDate().toString()
                                                                : null,
                                                0,
                                                "OUT_OF_STOCK")));

                // SUNAT alerts
                List<DashboardAlertsResponse.SunatAlert> sunatAlerts = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && (s.getSunatStatus() == Sale.SunatStatus.PENDING
                                                                || s.getSunatStatus() == Sale.SunatStatus.REJECTED)
                                                && s.getDate().isBefore(yesterday))
                                .map(s -> new DashboardAlertsResponse.SunatAlert(
                                                s.getId(),
                                                s.getDocumentType().name(),
                                                s.getSeries(),
                                                s.getNumber(),
                                                s.getSunatStatus().name(),
                                                s.getSunatMessage() != null ? s.getSunatMessage() : "Pendiente de envío"))
                                .collect(Collectors.toList());

                return new DashboardAlertsResponse(stockAlerts, sunatAlerts);
        }

        // ───────────────────────────────────────────────
        //  PAYMENT METHODS (distribution)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<PaymentMethodDistribution> getPaymentMethods(LocalDate date, Long establishmentId) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                List<SalePayment> payments = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(start)
                                                && s.getDate().isBefore(end))
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
        //  TOP PRODUCTS
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<TopProductDashboard> getTopProducts(int limit, Long establishmentId) {
                LocalDate today = LocalDate.now();
                LocalDateTime start = today.atStartOfDay();
                LocalDateTime end = today.atTime(LocalTime.MAX);

                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(start)
                                                && s.getDate().isBefore(end))
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(SaleItem::getProduct))
                                .entrySet().stream()
                                .map(e -> {
                                        BigDecimal qty = e.getValue().stream()
                                                        .map(SaleItem::getQuantity)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal amt = e.getValue().stream()
                                                        .map(SaleItem::getAmount)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        return new TopProductDashboard(
                                                        e.getKey().getId(),
                                                        e.getKey().getTradeName(),
                                                        qty, amt);
                                })
                                .sorted((a, b) -> b.quantitySold().compareTo(a.quantitySold()))
                                .limit(limit)
                                .collect(Collectors.toList());
        }

        // ───────────────────────────────────────────────
        //  EMPLOYEE PERFORMANCE
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<EmployeePerformanceDashboard> getEmployeePerformance(LocalDate date, Long establishmentId) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(start)
                                                && s.getDate().isBefore(end))
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
        //  SALES BY CATEGORY (donut chart)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<SalesByCategoryResponse> getSalesByCategory(String range, Long establishmentId) {
                int days = "30days".equalsIgnoreCase(range) ? 30 : 7;
                LocalDate now = LocalDate.now();
                LocalDateTime rangeStart = now.minusDays(days - 1).atStartOfDay();
                LocalDateTime rangeEnd = now.atTime(LocalTime.MAX);

                List<SaleItem> items = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(rangeStart)
                                                && s.getDate().isBefore(rangeEnd))
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.toList());

                BigDecimal grandTotal = items.stream()
                                .map(SaleItem::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Group by category
                Map<Category, BigDecimal> byCategory = items.stream()
                                .collect(Collectors.groupingBy(
                                                item -> {
                                                        Category cat = item.getProduct().getCategory();
                                                        return cat != null ? cat : createUncategorized();
                                                },
                                                Collectors.reducing(BigDecimal.ZERO, SaleItem::getAmount,
                                                                BigDecimal::add)));

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
                c.setName("Sin categoría");
                return c;
        }

        // ───────────────────────────────────────────────
        //  RECENT SALES
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<RecentSaleResponse> getRecentSales(int limit, Long establishmentId) {
                LocalDate today = LocalDate.now();
                LocalDateTime start = today.atStartOfDay();
                LocalDateTime end = today.atTime(LocalTime.MAX);

                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(start)
                                                && s.getDate().isBefore(end))
                                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                                .limit(limit)
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
                if (name == null || name.isBlank())
                        return "??";
                String[] parts = name.trim().split("\\s+");
                if (parts.length >= 2) {
                        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
                }
                return (parts[0].length() >= 2 ? parts[0].substring(0, 2) : parts[0]).toUpperCase();
        }

        // ───────────────────────────────────────────────
        //  EXPIRING LOTS
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<ExpiringLotResponse> getExpiringLots(int days, Long establishmentId) {
                LocalDate now = LocalDate.now();
                LocalDate limit = now.plusDays(days);

                return inventoryRepository.findAll().stream()
                                .filter(inv -> inv.getEstablishment().getId().equals(establishmentId)
                                                && inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                                && inv.getLot().getExpiryDate() != null
                                                && !inv.getLot().getExpiryDate().isBefore(now)
                                                && inv.getLot().getExpiryDate().isBefore(limit))
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
        //  LOW STOCK ITEMS
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<LowStockItemResponse> getLowStockItems(int limit, Long establishmentId) {
                return inventoryRepository.findAll().stream()
                                .filter(inv -> inv.getEstablishment().getId().equals(establishmentId)
                                                && inv.getMinStock() != null
                                                && inv.getMinStock() > 0
                                                && inv.getQuantity().compareTo(new BigDecimal(inv.getMinStock())) <= 0)
                                .sorted(Comparator.comparingDouble(inv -> {
                                        double level = inv.getQuantity().doubleValue() / inv.getMinStock();
                                        return level; // ascending = most critical first
                                }))
                                .limit(limit)
                                .map(inv -> {
                                        int current = inv.getQuantity().intValue();
                                        int min = inv.getMinStock();
                                        double level = min > 0 ? (double) current / min : 0.0;
                                        String categoryName = "Sin categoría";
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
        //  FULL DASHBOARD (unified endpoint)
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
