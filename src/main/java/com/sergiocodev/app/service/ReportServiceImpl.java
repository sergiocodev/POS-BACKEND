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
        private final PurchaseRepository purchaseRepository;
        private final InventoryRepository inventoryRepository;
        private final DocumentSequenceRepository documentSequenceRepository;

        public ReportServiceImpl(SaleRepository saleRepository, PurchaseRepository purchaseRepository,
                        InventoryRepository inventoryRepository, DocumentSequenceRepository documentSequenceRepository) {
                this.saleRepository = saleRepository;
                this.purchaseRepository = purchaseRepository;
                this.inventoryRepository = inventoryRepository;
                this.documentSequenceRepository = documentSequenceRepository;
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
                c.setName("Sin Categoría");
                return c;
        }

        // ── Nuevos reportes para tabs de farmacia ──

        @Override
        @Transactional(readOnly = true)
        public List<SalesReport> getSalesFiltered(LocalDate start, LocalDate end, Long establishmentId,
                        Sale.SaleDocumentType documentType, String series) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findByFilters(establishmentId, startTime, endTime,
                                documentType, series);

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
        public List<SalesBySeriesReport> getSalesBySeries(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findAllForReport(establishmentId, startTime, endTime,
                                Pageable.unpaged()).getContent();

                // Group by documentType + series
                return sales.stream()
                                .collect(Collectors.groupingBy(
                                                s -> s.getDocumentType().name() + "|" + s.getSeries()))
                                .entrySet().stream()
                                .map(entry -> {
                                        String[] parts = entry.getKey().split("\\|", 2);
                                        String docType = parts[0];
                                        String seriesVal = parts.length > 1 ? parts[1] : "";
                                        List<Sale> group = entry.getValue();

                                        List<Sale> valid = group.stream().filter(s -> !s.isVoided())
                                                        .collect(Collectors.toList());
                                        List<Sale> voided = group.stream().filter(Sale::isVoided)
                                                        .collect(Collectors.toList());

                                        BigDecimal totalSubTotal = valid.stream().map(Sale::getSubTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalTax = valid.stream().map(Sale::getTax)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalAmount = valid.stream().map(Sale::getTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal voidedAmount = voided.stream().map(Sale::getTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        return new SalesBySeriesReport(docType, seriesVal,
                                                        (long) valid.size(), totalSubTotal, totalTax,
                                                        totalAmount, (long) voided.size(), voidedAmount);
                                })
                                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByPaymentMethodReport> getSalesByPaymentMethod(LocalDate start, LocalDate end,
                        Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findWithPaymentsForReport(establishmentId, startTime, endTime);

                // Flatten all payments from non-voided sales
                List<SalePayment> allPayments = sales.stream()
                                .filter(s -> !s.isVoided())
                                .flatMap(s -> s.getPayments().stream())
                                .filter(p -> p.getDeletedAt() == null)
                                .collect(Collectors.toList());

                BigDecimal grandTotal = allPayments.stream()
                                .map(SalePayment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return allPayments.stream()
                                .collect(Collectors.groupingBy(p -> p.getPaymentMethod().name()))
                                .entrySet().stream()
                                .map(entry -> {
                                        BigDecimal totalAmount = entry.getValue().stream()
                                                        .map(SalePayment::getAmount)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                                                        ? totalAmount.multiply(new BigDecimal("100"))
                                                                        .divide(grandTotal, 2, RoundingMode.HALF_UP)
                                                        : BigDecimal.ZERO;
                                        return new SalesByPaymentMethodReport(
                                                        entry.getKey(),
                                                        (long) entry.getValue().size(),
                                                        totalAmount,
                                                        percentage);
                                })
                                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByLaboratoryReport> getSalesByLaboratory(LocalDate start, LocalDate end,
                        Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                return sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(
                                                item -> {
                                                        Laboratory lab = item.getProduct().getLaboratory();
                                                        return lab != null ? lab : createUnknownLaboratory();
                                                }))
                                .entrySet().stream()
                                .map(entry -> {
                                        BigDecimal revenue = entry.getValue().stream()
                                                        .map(SaleItem::getAmount)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal qty = entry.getValue().stream()
                                                        .map(SaleItem::getQuantity)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        long productCount = entry.getValue().stream()
                                                        .map(item -> item.getProduct().getId())
                                                        .distinct().count();
                                        return new SalesByLaboratoryReport(
                                                        entry.getKey().getId(),
                                                        entry.getKey().getName(),
                                                        revenue, qty, productCount);
                                })
                                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByEmployeeCategoryReport> getSalesByEmployeeCategory(LocalDate start, LocalDate end,
                        Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                // Cross-tab: employee × category
                return sales.stream()
                                .filter(s -> s.getUser() != null)
                                .flatMap(s -> s.getItems().stream()
                                                .map(item -> Map.entry(s, item)))
                                .collect(Collectors.groupingBy(
                                                entry -> {
                                                        User user = entry.getKey().getUser();
                                                        Category cat = entry.getValue().getProduct().getCategory();
                                                        String catName = cat != null ? cat.getName() : "Sin Categoría";
                                                        return user.getId() + "|" + user.getFullName() + "|" + catName;
                                                }))
                                .entrySet().stream()
                                .map(entry -> {
                                        String[] parts = entry.getKey().split("\\|", 3);
                                        Long userId = Long.parseLong(parts[0]);
                                        String userName = parts[1];
                                        String categoryName = parts[2];

                                        BigDecimal revenue = entry.getValue().stream()
                                                        .map(e -> e.getValue().getAmount())
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal qty = entry.getValue().stream()
                                                        .map(e -> e.getValue().getQuantity())
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        long txCount = entry.getValue().stream()
                                                        .map(e -> e.getKey().getId())
                                                        .distinct().count();

                                        return new SalesByEmployeeCategoryReport(
                                                        userId, userName, categoryName, revenue, qty, txCount);
                                })
                                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByCategoryDetailReport> getSalesByCategoryDetail(LocalDate start, LocalDate end,
                        Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                // Group items by category, then sub-group by product
                Map<Category, List<SaleItem>> itemsByCategory = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(
                                                item -> {
                                                        Category cat = item.getProduct().getCategory();
                                                        return cat != null ? cat : createUncategorized();
                                                }));

                return itemsByCategory.entrySet().stream()
                                .map(entry -> {
                                        Category cat = entry.getKey();
                                        List<SaleItem> items = entry.getValue();

                                        BigDecimal totalRevenue = items.stream()
                                                        .map(SaleItem::getAmount)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalQty = items.stream()
                                                        .map(SaleItem::getQuantity)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        // Sub-group by product
                                        List<SalesByCategoryDetailReport.ProductDetail> products = items.stream()
                                                        .collect(Collectors.groupingBy(
                                                                        item -> item.getProduct().getId()))
                                                        .entrySet().stream()
                                                        .map(productEntry -> {
                                                                List<SaleItem> productItems = productEntry.getValue();
                                                                Product product = productItems.get(0).getProduct();
                                                                BigDecimal pQty = productItems.stream()
                                                                                .map(SaleItem::getQuantity)
                                                                                .reduce(BigDecimal.ZERO,
                                                                                                BigDecimal::add);
                                                                BigDecimal pRevenue = productItems.stream()
                                                                                .map(SaleItem::getAmount)
                                                                                .reduce(BigDecimal.ZERO,
                                                                                                BigDecimal::add);
                                                                String labName = product.getLaboratory() != null
                                                                                ? product.getLaboratory().getName()
                                                                                : "N/A";
                                                                return new SalesByCategoryDetailReport.ProductDetail(
                                                                                product.getId(),
                                                                                product.getTradeName(),
                                                                                labName,
                                                                                pQty, pRevenue);
                                                        })
                                                        .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                                                        .collect(Collectors.toList());

                                        return new SalesByCategoryDetailReport(
                                                        cat.getId(), cat.getName(), totalRevenue, totalQty,
                                                        (long) products.size(), products);
                                })
                                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                                .collect(Collectors.toList());
        }

        private Laboratory createUnknownLaboratory() {
                Laboratory l = new Laboratory();
                l.setId(0L);
                l.setName("Sin Laboratorio");
                return l;
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByProductReport> getSalesByProduct(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                // Group items by product
                Map<Product, List<SaleItem>> itemsByProduct = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(SaleItem::getProduct));

                return itemsByProduct.entrySet().stream()
                                .map(entry -> {
                                        Product product = entry.getKey();
                                        List<SaleItem> items = entry.getValue();

                                        BigDecimal totalRevenue = items.stream()
                                                        .map(SaleItem::getAmount)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        Long quantitySold = items.stream()
                                                        .map(i -> i.getQuantity().longValue())
                                                        .reduce(0L, Long::sum);

                                        String catName = product.getCategory() != null ? product.getCategory().getName() : "Sin Categoría";
                                        String labName = product.getLaboratory() != null ? product.getLaboratory().getName() : "Sin Laboratorio";

                                        return new SalesByProductReport(
                                                        product.getId(),
                                                        product.getTradeName(),
                                                        catName,
                                                        labName,
                                                        quantitySold,
                                                        totalRevenue);
                                })
                                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<String> getAvailableSeries(Long establishmentId, Sale.SaleDocumentType documentType) {
                DocumentSequence.DocumentType seqDocType;
                try {
                        seqDocType = DocumentSequence.DocumentType.valueOf(documentType.name());
                } catch (Exception e) {
                        return List.of();
                }
                return documentSequenceRepository.findSeriesByEstablishmentAndDocumentType(establishmentId, seqDocType);
        }
}

