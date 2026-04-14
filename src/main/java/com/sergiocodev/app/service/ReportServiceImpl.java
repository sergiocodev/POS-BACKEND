package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

        private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

        private final SaleRepository saleRepository;
        private final InventoryRepository inventoryRepository;
        private final PurchaseRepository purchaseRepository;

        public ReportServiceImpl(SaleRepository saleRepository, InventoryRepository inventoryRepository,
                        PurchaseRepository purchaseRepository) {
                this.saleRepository = saleRepository;
                this.inventoryRepository = inventoryRepository;
                this.purchaseRepository = purchaseRepository;
        }

        @Override
        @Transactional(readOnly = true)
        public DailySalesReport getDailySales(LocalDate date, Long establishmentId) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                // Query at DB level instead of loading all sales
                List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                establishmentId, start, end, Pageable.unpaged()).getContent();

                BigDecimal totalSales = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalTax = sales.stream().map(Sale::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

                return new DailySalesReport(date, (long) sales.size(), totalSales, totalTax, BigDecimal.ZERO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProfitabilityReport> getProfitability(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                // Query at DB level
                List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                establishmentId, startTime, endTime, Pageable.unpaged()).getContent();

                Map<Long, List<SaleItem>> itemsByProduct = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

                // Pre-fetch inventory costs in a single query to avoid N+1
                Map<Long, BigDecimal> lotCostMap = inventoryRepository.findSummaryByEstablishment(establishmentId)
                                .stream()
                                .collect(Collectors.toMap(
                                                inv -> inv.getLot().getId(),
                                                inv -> inv.getCostPrice() != null ? inv.getCostPrice() : BigDecimal.ZERO,
                                                (existing, replacement) -> existing));

                List<ProfitabilityReport> reports = new ArrayList<>();
                for (var entry : itemsByProduct.entrySet()) {
                        List<SaleItem> items = entry.getValue();
                        String name = items.get(0).getProduct().getTradeName();
                        BigDecimal qty = items.stream().map(SaleItem::getQuantity).reduce(BigDecimal.ZERO,
                                        BigDecimal::add);
                        BigDecimal revenue = items.stream().map(SaleItem::getAmount).reduce(BigDecimal.ZERO,
                                        BigDecimal::add);

                        // Calculate cost using pre-fetched inventory cost map
                        BigDecimal cost = items.stream()
                                        .map(item -> {
                                                Long lotId = item.getLot() != null ? item.getLot().getId() : null;
                                                BigDecimal unitCost = lotId != null
                                                                ? lotCostMap.getOrDefault(lotId, BigDecimal.ZERO)
                                                                : BigDecimal.ZERO;
                                                if (unitCost.compareTo(BigDecimal.ZERO) == 0) {
                                                        log.debug("No cost price found for product {} in lot {}, using zero cost",
                                                                        item.getProduct().getId(), lotId);
                                                }
                                                return unitCost.multiply(item.getQuantity());
                                        })
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal profit = revenue.subtract(cost);
                        BigDecimal margin = revenue.compareTo(BigDecimal.ZERO) > 0
                                        ? profit.multiply(new BigDecimal("100")).divide(revenue, 2,
                                                        RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO;

                        reports.add(new ProfitabilityReport(entry.getKey(), name, qty, revenue, cost, profit, margin));
                }

                log.info("Profitability report generated: {} products, establishmentId={}", reports.size(), establishmentId);
                return reports;
        }

        @Override
        @Transactional(readOnly = true)
        public List<SunatStatusReport> getSunatStatus(Long establishmentId) {
                // Query at DB level with lightweight projection
                return saleRepository.findForReports(establishmentId,
                                LocalDate.now().minusYears(1).atStartOfDay(),
                                LocalDateTime.now(), Pageable.unpaged())
                                .getContent().stream()
                                .collect(Collectors.groupingBy(Sale::getSunatStatus, Collectors.counting()))
                                .entrySet().stream()
                                .map(e -> new SunatStatusReport(e.getKey(), e.getValue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<TopProductReport> getTopProducts(LocalDate start, LocalDate end, Long establishmentId,
                        String sortBy, int limit) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                // Use the aggregate query for better performance
                List<Object[]> topProducts = saleRepository.findTopProductsByQuantity(
                                establishmentId, startTime, endTime, Pageable.ofSize(200));

                if (topProducts.isEmpty()) {
                        return new ArrayList<>();
                }

                // Calculate totals for percentage
                BigDecimal grandTotal = topProducts.stream()
                                .map(row -> (BigDecimal) row[2])
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<TopProductReport> sorted = topProducts.stream()
                                .map(row -> {
                                        Long productId = (Long) row[0];
                                        BigDecimal value = "quantity".equalsIgnoreCase(sortBy) ? (BigDecimal) row[1] : (BigDecimal) row[2];
                                        BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                                                        ? ((BigDecimal) row[2]).multiply(new BigDecimal("100"))
                                                                        .divide(grandTotal, 2, RoundingMode.HALF_UP)
                                                        : BigDecimal.ZERO;
                                        // Product name needs a lookup - keep it simple with a single query
                                        return new TopProductReport(productId, "Product #" + productId, value, percentage,
                                                        BigDecimal.ZERO);
                                })
                                .sorted((a, b) -> b.value().compareTo(a.value()))
                                .collect(Collectors.toList());

                // Enrich with product names
                enrichProductNames(sorted, establishmentId, startTime, endTime);

                List<TopProductReport> withCumulative = new ArrayList<>();
                BigDecimal cumulative = BigDecimal.ZERO;
                for (var r : sorted) {
                        cumulative = cumulative.add(r.percentage());
                        withCumulative.add(new TopProductReport(
                                        r.productId(),
                                        r.productName(),
                                        r.value(),
                                        r.percentage(),
                                        cumulative));
                }

                return withCumulative.stream().limit(limit).collect(Collectors.toList());
        }

        private void enrichProductNames(List<TopProductReport> reports, Long establishmentId,
                        LocalDateTime startTime, LocalDateTime endTime) {
                try {
                        List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                        establishmentId, startTime, endTime, Pageable.unpaged()).getContent();
                        Map<Long, String> nameMap = sales.stream()
                                        .flatMap(s -> s.getItems().stream())
                                        .collect(Collectors.toMap(
                                                        item -> item.getProduct().getId(),
                                                        item -> item.getProduct().getTradeName(),
                                                        (existing, replacement) -> existing));
                        for (int i = 0; i < reports.size(); i++) {
                                TopProductReport report = reports.get(i);
                                String name = nameMap.get(report.productId());
                                if (name != null) {
                                        reports.set(i, new TopProductReport(
                                                        report.productId(), name, report.value(),
                                                        report.percentage(), report.cumulativePercentage()));
                                }
                        }
                } catch (Exception e) {
                        log.warn("Failed to enrich product names for top products report", e);
                }
        }

        @Override
        @Transactional(readOnly = true)
        public List<CategorySalesReport> getSalesByCategory(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                // Use repository query with eager-loaded items+product
                List<Sale> sales = saleRepository.findForCategoryAnalysis(establishmentId, startTime, endTime);

                return sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(
                                                item -> {
                                                        Category cat = item.getProduct().getCategory();
                                                        return cat != null ? cat : createUncategorized();
                                                },
                                                Collectors.toList()))
                                .entrySet().stream()
                                .map(e -> {
                                        BigDecimal revenue = e.getValue().stream().map(SaleItem::getAmount)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal qty = e.getValue().stream().map(SaleItem::getQuantity)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        return new CategorySalesReport(e.getKey().getId(), e.getKey().getName(),
                                                        revenue, qty);
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<EmployeeSalesReport> getSalesByEmployee(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                // Use repository query with eager-loaded items
                List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeForEmployee(
                                establishmentId, startTime, endTime);

                return sales.stream()
                                .collect(Collectors.groupingBy(Sale::getUser))
                                .entrySet().stream()
                                .filter(e -> e.getKey() != null)
                                .map(e -> {
                                        BigDecimal revenue = e.getValue().stream().map(Sale::getTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal qty = e.getValue().stream()
                                                        .flatMap(s -> s.getItems().stream())
                                                        .map(SaleItem::getQuantity)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        return new EmployeeSalesReport(e.getKey().getId(), e.getKey().getFullName(),
                                                        revenue, qty, (long) e.getValue().size());
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<HourlyHeatReport> getHourlyHeat(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                // Query at DB level
                List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                establishmentId, startTime, endTime, Pageable.unpaged()).getContent();

                Map<Integer, List<Sale>> byHour = sales.stream()
                                .collect(Collectors.groupingBy(s -> s.getDate().getHour()));

                List<HourlyHeatReport> result = new ArrayList<>();
                for (int i = 0; i < 24; i++) {
                        List<Sale> hourSales = byHour.getOrDefault(i, new ArrayList<>());
                        BigDecimal revenue = hourSales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO,
                                        BigDecimal::add);
                        result.add(new HourlyHeatReport(i, revenue, (long) hourSales.size()));
                }
                return result;
        }

        @Override
        @Transactional(readOnly = true)
        public List<LowRotationReport> getLowRotation(int days, Long establishmentId) {
                LocalDateTime threshold = LocalDateTime.now().minusDays(days);

                // Query inventory at DB level
                List<Inventory> inventoryList = inventoryRepository.findSummaryByEstablishment(establishmentId)
                                .stream()
                                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                                .collect(Collectors.toList());

                return inventoryList.stream()
                                .collect(Collectors.groupingBy(inv -> inv.getLot().getProduct()))
                                .entrySet().stream()
                                .map(e -> {
                                        Product p = e.getKey();
                                        BigDecimal stock = e.getValue().stream().map(Inventory::getQuantity)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        // This query is still in-memory but operates on the already-filtered sales set
                                        LocalDateTime lastSale = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                                        establishmentId, p.getCreatedAt(), LocalDateTime.now(),
                                                        Pageable.unpaged())
                                                        .getContent().stream()
                                                        .filter(s -> !s.isVoided())
                                                        .flatMap(s -> s.getItems().stream())
                                                        .filter(item -> item.getProduct().getId().equals(p.getId()))
                                                        .map(item -> item.getSale().getDate())
                                                        .max(LocalDateTime::compareTo)
                                                        .orElse(p.getCreatedAt());

                                        return new LowRotationReport(p.getId(), p.getTradeName(), lastSale, stock);
                                })
                                .filter(r -> r.lastSaleDate().isBefore(threshold))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<PurchaseReport> getPurchases(LocalDate start, LocalDate end, Long establishmentId) {
                // Use paginated repository query
                List<Purchase> purchases = purchaseRepository.findByEstablishmentAndDateRangeList(
                                establishmentId, start, end);

                return purchases.stream()
                                .map(p -> new PurchaseReport(
                                                p.getId(),
                                                p.getSupplier().getName(),
                                                p.getDocumentType().name(),
                                                (p.getSeries() != null ? p.getSeries() + "-" : "") +
                                                                (p.getNumber() != null ? p.getNumber() : ""),
                                                p.getIssueDate(),
                                                p.getArrivalDate(),
                                                p.getSubTotal(),
                                                p.getTax(),
                                                p.getTotal(),
                                                p.getStatus().name(),
                                                p.getItems().size()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesReport> getSales(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                // Use paginated repository query
                List<Sale> sales = saleRepository.findForReports(establishmentId, startTime, endTime,
                                Pageable.unpaged()).getContent();

                return sales.stream()
                                .map(s -> new SalesReport(
                                                s.getId(),
                                                s.getCustomer() != null ? s.getCustomer().getName()
                                                                : "Cliente General",
                                                s.getUser().getFullName(),
                                                s.getDocumentType().name(),
                                                s.getSeries() + "-" + s.getNumber(),
                                                s.getDate(),
                                                s.getSubTotal(),
                                                s.getTax(),
                                                s.getTotal(),
                                                s.getStatus().name(),
                                                s.getSunatStatus() != null ? s.getSunatStatus().name() : null,
                                                s.isVoided()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public SalesSummaryReport getSalesSummary(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                // Use repository query
                List<Sale> sales = saleRepository.findForReports(establishmentId, startTime, endTime,
                                Pageable.unpaged()).getContent();

                long totalTransactions = sales.stream().filter(s -> !s.isVoided()).count();
                BigDecimal totalRevenue = sales.stream().filter(s -> !s.isVoided())
                                .map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalTax = sales.stream().filter(s -> !s.isVoided())
                                .map(Sale::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

                long voidedCount = sales.stream().filter(Sale::isVoided).count();
                BigDecimal voidedAmount = sales.stream().filter(Sale::isVoided)
                                .map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Long> countByDocumentType = sales.stream().filter(s -> !s.isVoided())
                                .collect(Collectors.groupingBy(s -> s.getDocumentType().name(), Collectors.counting()));

                Map<String, BigDecimal> amountByDocumentType = sales.stream().filter(s -> !s.isVoided())
                                .collect(Collectors.groupingBy(s -> s.getDocumentType().name(),
                                                Collectors.reducing(BigDecimal.ZERO, Sale::getTotal, BigDecimal::add)));

                return new SalesSummaryReport(start, end, totalTransactions, totalRevenue, totalTax,
                                voidedCount, voidedAmount, countByDocumentType, amountByDocumentType);
        }

        private Category createUncategorized() {
                Category c = new Category();
                c.setId(0L);
                c.setName("Uncategorized");
                return c;
        }
}
