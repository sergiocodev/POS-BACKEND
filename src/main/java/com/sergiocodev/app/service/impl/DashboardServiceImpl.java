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
        private final CashSessionRepository cashSessionRepository;
        private final AccountReceivableRepository accountReceivableRepository;
        private final PurchaseRepository purchaseRepository;
        private final AccountPayableRepository accountPayableRepository;
        private final CashMovementRepository cashMovementRepository;

        public DashboardServiceImpl(SaleRepository saleRepository,
                        InventoryRepository inventoryRepository,
                        CashSessionRepository cashSessionRepository,
                        AccountReceivableRepository accountReceivableRepository,
                        PurchaseRepository purchaseRepository,
                        AccountPayableRepository accountPayableRepository,
                        CashMovementRepository cashMovementRepository) {
                this.saleRepository = saleRepository;
                this.inventoryRepository = inventoryRepository;
                this.cashSessionRepository = cashSessionRepository;
                this.accountReceivableRepository = accountReceivableRepository;
                this.purchaseRepository = purchaseRepository;
                this.accountPayableRepository = accountPayableRepository;
                this.cashMovementRepository = cashMovementRepository;
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
                BigDecimal totalToday = saleRepository.sumTotalByEstablishmentAndDateBetween(establishmentId,
                                startToday, now);
                if (totalToday == null)
                        totalToday = BigDecimal.ZERO;

                long countYesterday = saleRepository.countByEstablishmentAndDateBetween(establishmentId, startYesterday,
                                endYesterday);
                BigDecimal totalYesterday = saleRepository.sumTotalByEstablishmentAndDateBetween(establishmentId,
                                startYesterday, endYesterday);
                if (totalYesterday == null)
                        totalYesterday = BigDecimal.ZERO;

                String salesTrend = calculateTrend(totalToday, totalYesterday);
                String countTrend = calculateTrend(new BigDecimal(countToday), new BigDecimal(countYesterday));

                LocalDateTime yesterdayLimit = now.minusDays(1);

                long pendingSunat = saleRepository.countPendingSunat(
                                establishmentId,
                                List.of(Sale.SunatStatus.PENDING, Sale.SunatStatus.REJECTED),
                                yesterdayLimit);

                long expired = inventoryRepository.countExpiredLots(establishmentId, now.toLocalDate());
                long expiringSoon = inventoryRepository.countExpiringLots(establishmentId, now.toLocalDate(),
                                now.plusMonths(3).toLocalDate());
                long outOfStock = inventoryRepository.countOutOfStock(establishmentId);
                long totalProducts = inventoryRepository.countDistinctProductsInStock(establishmentId);

                DashboardSummaryResponse.ValueTrend salesVT = new DashboardSummaryResponse.ValueTrend(totalToday, "PEN",
                                salesTrend);
                DashboardSummaryResponse.ValueTrendLong countVT = new DashboardSummaryResponse.ValueTrendLong(
                                countToday, countTrend);
                DashboardSummaryResponse.StockAlertsData stockVT = new DashboardSummaryResponse.StockAlertsData(expired,
                                expiringSoon, outOfStock);

                BigDecimal cashBalance = cashSessionRepository.sumCalculatedBalanceOfOpenSessions(
                                com.sergiocodev.app.model.CashSession.SessionStatus.OPEN, establishmentId);
                if (cashBalance == null)
                        cashBalance = BigDecimal.ZERO;

                BigDecimal accountsReceivable = accountReceivableRepository.getTotalPendingBalance(
                                List.of(AccountReceivable.ReceivableStatus.PAID,
                                                AccountReceivable.ReceivableStatus.CANCELED),
                                establishmentId);
                if (accountsReceivable == null)
                        accountsReceivable = BigDecimal.ZERO;

                DashboardSummaryResponse.SummaryData data = new DashboardSummaryResponse.SummaryData(
                                salesVT, countVT, pendingSunat, stockVT, totalProducts, cashBalance,
                                accountsReceivable);

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
        // CASHFLOW CHART (income vs expense)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<CashflowChartResponse> getCashflowChart(String range, Long establishmentId) {
                int days = "30days".equalsIgnoreCase(range) ? 30 : 7;
                LocalDate now = LocalDate.now();

                List<CashflowChartResponse> result = new ArrayList<>();
                LocalDateTime rangeStart = now.minusDays(days - 1).atStartOfDay();

                Page<Sale> salesPage = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                establishmentId, rangeStart, now.atTime(LocalTime.MAX), Pageable.unpaged());
                List<Sale> salesInRange = salesPage.getContent();

                List<com.sergiocodev.app.model.Purchase> purchasesInRange = purchaseRepository
                                .findByEstablishmentAndDateRangeList(
                                                establishmentId, rangeStart.toLocalDate(), now);

                List<CashMovement> outMovementsInRange = cashMovementRepository.findExpensesByEstablishmentAndDateRange(
                                establishmentId, rangeStart, now.atTime(LocalTime.MAX));

                for (int i = 0; i < days; i++) {
                        LocalDate date = now.minusDays(days - 1 - i);

                        BigDecimal dayIncome = salesInRange.stream()
                                        .filter(s -> s.getDate().toLocalDate().equals(date))
                                        .map(Sale::getTotal)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal dayPurchaseExpense = purchasesInRange.stream()
                                        .filter(p -> p.getIssueDate().equals(date))
                                        .map(com.sergiocodev.app.model.Purchase::getTotal)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal dayCashExpense = outMovementsInRange.stream()
                                        .filter(m -> m.getCreatedAt().toLocalDate().equals(date))
                                        .map(CashMovement::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal dayExpense = dayPurchaseExpense.add(dayCashExpense);

                        result.add(new CashflowChartResponse(date, dayIncome, dayExpense));
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

                List<Inventory> expiringInventory = inventoryRepository.findExpiringLots(establishmentId, now,
                                threeMonthsFromNow);
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
                                                s.getSunatMessage() != null ? s.getSunatMessage()
                                                                : "Pendiente de env\u00edo"))
                                .collect(Collectors.toList());

                return new DashboardAlertsResponse(stockAlerts, sunatAlerts);
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
                                                Collectors.reducing(BigDecimal.ZERO, SaleItem::getQuantity,
                                                                BigDecimal::add)));

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

                                        String categoryName = p.getCategory() != null ? p.getCategory().getName()
                                                        : "Sin categor\u00eda";

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

                List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeForEmployee(establishmentId, start,
                                end);

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
                c.setName("Sin categor\u00eda");
                return c;
        }

        // ───────────────────────────────────────────────
        // RECENT TRANSACTIONS (Sales and Purchases)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<RecentTransactionResponse> getRecentTransactions(int limit, Long establishmentId) {
                LocalDate today = LocalDate.now();
                LocalDateTime start = today.atStartOfDay();
                LocalDateTime end = today.atTime(LocalTime.MAX);

                Page<Sale> salesPage = saleRepository.findRecentSales(establishmentId, start, end,
                                Pageable.ofSize(limit));
                Page<Purchase> purchasesPage = purchaseRepository.findByEstablishmentAndDateRange(establishmentId,
                                today, today, Pageable.ofSize(limit));

                List<RecentTransactionResponse> transactions = new java.util.ArrayList<>();

                transactions.addAll(salesPage.getContent().stream().map(s -> {
                        String entityName = "Cliente General";
                        String initials = "CG";
                        if (s.getCustomer() != null && s.getCustomer().getName() != null
                                        && !s.getCustomer().getName().isBlank()) {
                                entityName = s.getCustomer().getName();
                                initials = buildInitials(entityName);
                        }
                        int productCount = s.getItems() != null ? s.getItems().size() : 0;
                        return new RecentTransactionResponse(s.getId(), entityName, initials, "VENTA",
                                        s.getDocumentType().name(), productCount, s.getDate(), s.getTotal());
                }).collect(Collectors.toList()));

                transactions.addAll(purchasesPage.getContent().stream().map(p -> {
                        String entityName = "Proveedor General";
                        String initials = "PR";
                        if (p.getSupplier() != null && p.getSupplier().getName() != null
                                        && !p.getSupplier().getName().isBlank()) {
                                entityName = p.getSupplier().getName();
                                initials = buildInitials(entityName);
                        }
                        int productCount = p.getItems() != null ? p.getItems().size() : 0;
                        return new RecentTransactionResponse(p.getId(), entityName, initials, "COMPRA",
                                        p.getDocumentType().name(), productCount, p.getCreatedAt(), p.getTotal());
                }).collect(Collectors.toList()));

                transactions.sort((t1, t2) -> t2.date().compareTo(t1.date()));

                return transactions.stream().limit(limit).collect(Collectors.toList());
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
                Page<Inventory> lowStockPage = inventoryRepository.findLowStockItems(establishmentId,
                                Pageable.ofSize(limit));

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
        // SUNAT STATUS (distribution)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<SunatStatusDistribution> getSunatStatusDistribution(String range, Long establishmentId) {
                int days = "30days".equalsIgnoreCase(range) ? 30 : 7;
                LocalDateTime rangeStart = LocalDate.now().minusDays(days - 1).atStartOfDay();

                Page<Sale> salesPage = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                establishmentId, rangeStart, LocalDateTime.now().with(LocalTime.MAX),
                                Pageable.unpaged());
                List<Sale> sales = salesPage.getContent();

                BigDecimal grandTotal = sales.stream()
                                .map(Sale::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return sales.stream()
                                .collect(Collectors.groupingBy(Sale::getSunatStatus))
                                .entrySet().stream()
                                .map(e -> {
                                        BigDecimal amount = e.getValue().stream()
                                                        .map(Sale::getTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        long count = e.getValue().size();
                                        double percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                                                        ? amount.divide(grandTotal, 4, RoundingMode.HALF_UP)
                                                                        .multiply(new BigDecimal("100")).doubleValue()
                                                        : 0.0;
                                        return new SunatStatusDistribution(
                                                        e.getKey().name(), count, amount, percentage);
                                })
                                .sorted((a, b) -> Double.compare(b.percentage(), a.percentage()))
                                .collect(Collectors.toList());
        }

        // ───────────────────────────────────────────────
        // UPCOMING PAYABLES (Accounts Payable)
        // ───────────────────────────────────────────────
        @Override
        @Transactional(readOnly = true)
        public List<AccountPayableDashboardResponse> getUpcomingPayables(int limit, Long establishmentId) {
                Page<AccountPayable> payablesPage = accountPayableRepository.findUpcomingPayables(
                                List.of(AccountPayable.PayableStatus.PENDING, AccountPayable.PayableStatus.PARTIAL),
                                establishmentId, Pageable.ofSize(limit));

                LocalDate today = LocalDate.now();

                return payablesPage.getContent().stream()
                                .map(a -> new AccountPayableDashboardResponse(
                                                a.getId(),
                                                a.getSupplier().getName(),
                                                a.getPurchase().getSeries() + "-" + a.getPurchase().getNumber(),
                                                a.getPendingBalance(),
                                                a.getDueDate(),
                                                a.getDueDate() != null && a.getDueDate().isBefore(today)))
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
                getCashflowChart("7days", establishmentId),
                getSalesByCategory("7days", establishmentId),
                getTopProducts(5, establishmentId),
                getEmployeePerformance(today, establishmentId),
                getRecentTransactions(10, establishmentId),
                getLowStockItems(10, establishmentId),
                getExpiringLots(30, establishmentId),
                getSunatStatusDistribution("7days", establishmentId),
                getUpcomingPayables(10, establishmentId));
    }
}
