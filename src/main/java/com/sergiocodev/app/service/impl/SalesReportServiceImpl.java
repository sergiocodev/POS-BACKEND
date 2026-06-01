package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import com.sergiocodev.app.service.interfaces.SalesReportService;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SalesReportServiceImpl implements SalesReportService {

    private static final Logger log = LoggerFactory.getLogger(SalesReportServiceImpl.class);

    private final SaleRepository saleRepository;
    private final InventoryRepository inventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentSequenceRepository documentSequenceRepository;

    public SalesReportServiceImpl(SaleRepository saleRepository,
                                   InventoryRepository inventoryRepository,
                                   EmployeeRepository employeeRepository,
                                   DocumentSequenceRepository documentSequenceRepository) {
        this.saleRepository = saleRepository;
        this.inventoryRepository = inventoryRepository;
        this.employeeRepository = employeeRepository;
        this.documentSequenceRepository = documentSequenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DailySalesReport getDailySales(LocalDateTime date, Long establishmentId) {
        LocalDateTime start = date.toLocalDate().atStartOfDay();
        LocalDateTime end = date.toLocalDate().atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                establishmentId, start, end, Pageable.unpaged()).getContent();

        BigDecimal totalSales = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = sales.stream().map(Sale::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DailySalesReport(date.toLocalDate(), (long) sales.size(), totalSales, totalTax, BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfitabilityReport> getProfitability(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                establishmentId, start, end, Pageable.unpaged()).getContent();

        Map<Long, List<SaleItem>> itemsByProduct = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

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
            BigDecimal qty = items.stream().map(SaleItem::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal revenue = items.stream().map(SaleItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal cost = items.stream()
                    .map(item -> {
                        Long lotId = item.getLot() != null ? item.getLot().getId() : null;
                        BigDecimal unitCost = lotId != null
                                ? lotCostMap.getOrDefault(lotId, BigDecimal.ZERO) : BigDecimal.ZERO;
                        if (unitCost.compareTo(BigDecimal.ZERO) == 0) {
                            log.debug("No cost price found for product {} in lot {}, using zero cost",
                                    item.getProduct().getId(), lotId);
                        }
                        return unitCost.multiply(item.getQuantity());
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal profit = revenue.subtract(cost);
            BigDecimal margin = revenue.compareTo(BigDecimal.ZERO) > 0
                    ? profit.multiply(new BigDecimal("100")).divide(revenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            reports.add(new ProfitabilityReport(entry.getKey(), name, qty, revenue, cost, profit, margin));
        }

        log.info("Profitability report generated: {} products, establishmentId={}", reports.size(), establishmentId);
        return reports;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SunatStatusReport> getSunatStatus(Long establishmentId) {
        return saleRepository.findForReports(establishmentId,
                        LocalDate.now().minusYears(1).atStartOfDay(), LocalDateTime.now(), Pageable.unpaged())
                .getContent().stream()
                .collect(Collectors.groupingBy(Sale::getSunatStatus, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new SunatStatusReport(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductReport> getTopProducts(LocalDateTime start, LocalDateTime end, Long establishmentId,
                                                   String sortBy, int limit) {
        List<Object[]> topProducts = saleRepository.findTopProductsByQuantity(
                establishmentId, start, end, Pageable.ofSize(200));

        if (topProducts.isEmpty()) return new ArrayList<>();

        BigDecimal grandTotal = topProducts.stream()
                .map(row -> SalesReportHelper.toBigDecimal(row, 2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TopProductReport> sorted = topProducts.stream()
                .map(row -> {
                    Long productId = SalesReportHelper.toLong(row, 0);
                    BigDecimal value = "quantity".equalsIgnoreCase(sortBy) ? SalesReportHelper.toBigDecimal(row, 1) : SalesReportHelper.toBigDecimal(row, 2);
                    BigDecimal amount = SalesReportHelper.toBigDecimal(row, 2);
                    BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? amount.multiply(new BigDecimal("100")).divide(grandTotal, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new TopProductReport(productId, "Product #" + productId, value, percentage, BigDecimal.ZERO);
                })
                .sorted((a, b) -> b.value().compareTo(a.value()))
                .collect(Collectors.toList());

        enrichProductNames(sorted, establishmentId, start, end);

        List<TopProductReport> withCumulative = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        for (var r : sorted) {
            cumulative = cumulative.add(r.percentage());
            withCumulative.add(new TopProductReport(r.productId(), r.productName(), r.value(), r.percentage(), cumulative));
        }

        return withCumulative.stream().limit(limit).collect(Collectors.toList());
    }

    private void enrichProductNames(List<TopProductReport> reports, Long establishmentId,
                                     LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // Query ligera: solo productId → tradeName, evita cargar ventas completas
            List<Object[]> nameRows = saleRepository.findProductNameMapping(
                    establishmentId, startTime, endTime);
            Map<Long, String> nameMap = nameRows.stream()
                    .collect(Collectors.toMap(
                            row -> SalesReportHelper.toLong(row, 0),
                            row -> row[1] != null ? row[1].toString() : "",
                            (existing, replacement) -> existing));
            for (int i = 0; i < reports.size(); i++) {
                TopProductReport report = reports.get(i);
                String name = nameMap.get(report.productId());
                if (name != null) {
                    reports.set(i, new TopProductReport(report.productId(), name, report.value(),
                            report.percentage(), report.cumulativePercentage()));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to enrich product names for top products report", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySalesReport> getSalesByCategory(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findForCategoryAnalysis(establishmentId, start, end);

        return sales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> {
                            Category cat = item.getProduct().getCategory();
                            return cat != null ? cat : createUncategorized();
                        }, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal revenue = e.getValue().stream().map(SaleItem::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal qty = e.getValue().stream().map(SaleItem::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CategorySalesReport(e.getKey().getId(), e.getKey().getName(), revenue, qty);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSalesReport> getSalesByEmployee(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeForEmployee(establishmentId, start, end);

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
    public List<HourlyHeatReport> getHourlyHeat(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                establishmentId, start, end, Pageable.unpaged()).getContent();

        Map<Integer, List<Sale>> byHour = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getDate().getHour()));

        List<HourlyHeatReport> result = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            List<Sale> hourSales = byHour.getOrDefault(i, new ArrayList<>());
            BigDecimal revenue = hourSales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(new HourlyHeatReport(i, revenue, (long) hourSales.size()));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowRotationReport> getLowRotation(int days, Long establishmentId) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);

        List<Inventory> inventoryList = inventoryRepository.findSummaryByEstablishment(establishmentId)
                .stream()
                .filter(inv -> inv.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        // Pre-cargar última fecha de venta por producto en UNA SOLA CONSULTA agregada (evita N+1)
        Map<Long, LocalDateTime> lastSaleByProduct = saleRepository.findLastSaleDateByProduct(establishmentId)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> row[1] != null ? (LocalDateTime) row[1] : LocalDateTime.MIN,
                        (existing, replacement) -> existing.compareTo(replacement) > 0 ? existing : replacement));

        return inventoryList.stream()
                .collect(Collectors.groupingBy(inv -> inv.getLot().getProduct()))
                .entrySet().stream()
                .map(e -> {
                    Product p = e.getKey();
                    BigDecimal stock = e.getValue().stream().map(Inventory::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    LocalDateTime lastSale = lastSaleByProduct.getOrDefault(
                            p.getId(), p.getCreatedAt());

                    return new LowRotationReport(p.getId(), p.getTradeName(), lastSale, stock);
                })
                .filter(r -> r.lastSaleDate().isBefore(threshold))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesReport> getSales(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findForReports(establishmentId, start, end, Pageable.unpaged()).getContent();

        return sales.stream()
                .map(SalesReportHelper::toSalesReport)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesSummaryReport getSalesSummary(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findForReports(establishmentId, start, end, Pageable.unpaged()).getContent();

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

        return new SalesSummaryReport(start.toLocalDate(), end.toLocalDate(), totalTransactions,
                totalRevenue, totalTax, voidedCount, voidedAmount, countByDocumentType, amountByDocumentType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesReport> getSalesFiltered(LocalDateTime start, LocalDateTime end, Long establishmentId,
                                               Sale.SaleDocumentType documentType, String series, Long sellerId) {
        List<Sale> sales = saleRepository.findByFilters(establishmentId, start, end, documentType, series);

        if (sellerId != null) {
            sales = sales.stream().filter(s -> s.getUser() != null && s.getUser().getId().equals(sellerId))
                    .collect(Collectors.toList());
        }

        return sales.stream()
                .map(SalesReportHelper::toSalesReport)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesBySeriesReport> getSalesBySeries(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findAllForReport(establishmentId, start, end, Pageable.unpaged()).getContent();

        return SalesReportHelper.buildSeriesReports(sales);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByPaymentMethodReport> getSalesByPaymentMethod(LocalDateTime start, LocalDateTime end,
                                                                      Long establishmentId) {
        List<Sale> sales = saleRepository.findWithPaymentsForReport(establishmentId, start, end);

        List<SalePayment> allPayments = sales.stream()
                .filter(s -> !s.isVoided())
                .flatMap(s -> s.getPayments().stream())
                .filter(p -> p.getDeletedAt() == null)
                .collect(Collectors.toList());

        BigDecimal grandTotal = allPayments.stream()
                .map(SalePayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return allPayments.stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentMethod().name()))
                .entrySet().stream()
                .map(entry -> {
                    BigDecimal totalAmount = entry.getValue().stream()
                            .map(SalePayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? totalAmount.multiply(new BigDecimal("100")).divide(grandTotal, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new SalesByPaymentMethodReport(entry.getKey(), (long) entry.getValue().size(),
                            totalAmount, percentage);
                })
                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByLaboratoryReport> getSalesByLaboratory(LocalDateTime start, LocalDateTime end,
                                                                Long establishmentId) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        return sales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(item -> {
                    Laboratory lab = item.getProduct().getLaboratory();
                    return lab != null ? lab : createUnknownLaboratory();
                }))
                .entrySet().stream()
                .map(entry -> {
                    BigDecimal revenue = entry.getValue().stream().map(SaleItem::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal qty = entry.getValue().stream().map(SaleItem::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long productCount = entry.getValue().stream()
                            .map(item -> item.getProduct().getId()).distinct().count();
                    return new SalesByLaboratoryReport(entry.getKey().getId(), entry.getKey().getName(),
                            revenue, qty, productCount);
                })
                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByEmployeeCategoryReport> getSalesByEmployeeCategory(LocalDateTime start, LocalDateTime end,
                                                                            Long establishmentId) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        return sales.stream()
                .filter(s -> s.getUser() != null)
                .flatMap(s -> s.getItems().stream().map(item -> Map.entry(s, item)))
                .collect(Collectors.groupingBy(entry -> {
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
                            .map(e -> e.getValue().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal qty = entry.getValue().stream()
                            .map(e -> e.getValue().getQuantity()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long txCount = entry.getValue().stream()
                            .map(e -> e.getKey().getId()).distinct().count();

                    return new SalesByEmployeeCategoryReport(userId, userName, categoryName, revenue, qty, txCount);
                })
                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByCategoryDetailReport> getSalesByCategoryDetail(LocalDateTime start, LocalDateTime end,
                                                                        Long establishmentId) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        Map<Category, List<SaleItem>> itemsByCategory = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(item -> {
                    Category cat = item.getProduct().getCategory();
                    return cat != null ? cat : createUncategorized();
                }));

        return SalesReportHelper.buildCategoryDetailReports(itemsByCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByProductReport> getSalesByProduct(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        Map<Product, List<SaleItem>> itemsByProduct = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(SaleItem::getProduct));

        return SalesReportHelper.buildProductReports(itemsByProduct, SalesReportHelper::labFromLaboratory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableSeries(Long establishmentId, Sale.SaleDocumentType documentType) {
        if (documentType == null) {
            return documentSequenceRepository.findSeriesByEstablishment(establishmentId);
        }
        try {
            DocumentSequence.DocumentType seqDocType = DocumentSequence.DocumentType.valueOf(documentType.name());
            return documentSequenceRepository.findSeriesByEstablishmentAndDocumentType(establishmentId, seqDocType);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByCategoryDetailReport> getSalesByCategories(LocalDateTime start, LocalDateTime end,
                                                                    Long establishmentId, List<Long> categoryIds, Long sellerId) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        if (sellerId != null) {
            sales = sales.stream().filter(s -> s.getUser() != null && s.getUser().getId().equals(sellerId))
                    .collect(Collectors.toList());
        }

        Map<Category, List<SaleItem>> itemsByCategory = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> {
                    Category cat = item.getProduct().getCategory();
                    if (categoryIds == null || categoryIds.isEmpty()) return true;
                    return cat != null && categoryIds.contains(cat.getId());
                })
                .collect(Collectors.groupingBy(item -> {
                    Category cat = item.getProduct().getCategory();
                    return cat != null ? cat : createUncategorized();
                }));

        return SalesReportHelper.buildCategoryDetailReports(itemsByCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByProductReport> getSalesByProductFilters(LocalDateTime start, LocalDateTime end,
                                                                Long establishmentId, List<Long> productIds,
                                                                List<Long> brandIds, List<Long> therapeuticActionIds,
                                                                Long sellerId) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        if (sellerId != null) {
            sales = sales.stream().filter(s -> s.getUser() != null && s.getUser().getId().equals(sellerId))
                    .collect(Collectors.toList());
        }

        Map<Product, List<SaleItem>> itemsByProduct = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .filter(item -> {
                    Product p = item.getProduct();
                    boolean matchProduct = productIds == null || productIds.isEmpty() || productIds.contains(p.getId());
                    boolean matchBrand = brandIds == null || brandIds.isEmpty()
                            || (p.getBrand() != null && brandIds.contains(p.getBrand().getId()));
                    boolean matchTherapeutic = therapeuticActionIds == null || therapeuticActionIds.isEmpty()
                            || p.getTherapeuticActions().stream().anyMatch(ta -> therapeuticActionIds.contains(ta.getId()));
                    return matchProduct && matchBrand && matchTherapeutic;
                })
                .collect(Collectors.groupingBy(SaleItem::getProduct));

        return SalesReportHelper.buildProductReports(itemsByProduct, SalesReportHelper::labFromBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesBySeriesReport> getSalesBySeriesFiltered(LocalDateTime start, LocalDateTime end,
                                                               Long establishmentId, List<String> seriesList) {
        List<Sale> sales = saleRepository.findAllForReport(establishmentId, start, end, Pageable.unpaged()).getContent();

        List<Sale> filtered = seriesList == null || seriesList.isEmpty()
                ? sales
                : sales.stream().filter(s -> seriesList.contains(s.getSeries())).collect(Collectors.toList());
        return SalesReportHelper.buildSeriesReports(filtered);
    }

    // ── Seller-specific reports ──

    @Override
    @Transactional(readOnly = true)
    public List<SalesReport> getSalesBySeller(LocalDateTime start, LocalDateTime end, Long establishmentId,
                                               List<Long> sellerIds) {
        List<Long> userIds = SalesReportHelper.mapEmployeeIdsToUserIds(sellerIds, employeeRepository);
        if (sellerIds != null && !sellerIds.isEmpty() && !sellerIds.contains(0L) && (userIds == null || userIds.isEmpty())) {
            return new ArrayList<>();
        }

        List<Sale> sales = saleRepository.findByFilters(establishmentId, start, end, null, null);

        if (userIds != null) {
            final List<Long> finalUserIds = userIds;
            sales = sales.stream()
                    .filter(s -> s.getUser() != null && finalUserIds.contains(s.getUser().getId()))
                    .collect(Collectors.toList());
        }

        return sales.stream()
                .map(s -> {
                    String employeeName = "N/A";
                    if (s.getUser() != null) {
                        Employee emp = employeeRepository.findByUserId(s.getUser().getId()).orElse(null);
                        if (emp != null) {
                            employeeName = emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "");
                        } else {
                            employeeName = s.getUser().getFullName();
                        }
                    }
                    return SalesReportHelper.toSalesReport(s, employeeName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getSellerNames(List<Long> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return "TODOS LOS VENDEDORES";
        }
        if (sellerIds.size() == 1 && !sellerIds.contains(0L)) {
            return employeeRepository.findById(sellerIds.get(0))
                    .map(e -> e.getFirstName() + " " + (e.getLastName() != null ? e.getLastName() : ""))
                    .orElse("VENDEDOR");
        }
        if (sellerIds.contains(0L)) {
            return "TODOS LOS VENDEDORES";
        }
        return "VARIOS VENDEDORES";
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByCategoryDetailReport> getSalesBySellerCategories(LocalDateTime start, LocalDateTime end,
                                                                          Long establishmentId, List<Long> sellerIds,
                                                                          List<Long> categoryIds) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        List<Long> userIds = SalesReportHelper.mapEmployeeIdsToUserIds(sellerIds, employeeRepository);
        final List<Long> finalSellerUserIds = userIds;

        Map<Category, List<SaleItem>> itemsByCategory = sales.stream()
                .filter(s -> finalSellerUserIds == null || (s.getUser() != null && finalSellerUserIds.contains(s.getUser().getId())))
                .flatMap(s -> s.getItems().stream())
                .filter(item -> {
                    Category cat = item.getProduct().getCategory();
                    if (categoryIds == null || categoryIds.isEmpty()) return true;
                    return cat != null && categoryIds.contains(cat.getId());
                })
                .collect(Collectors.groupingBy(item -> {
                    Category cat = item.getProduct().getCategory();
                    return cat != null ? cat : SalesReportHelper.createUncategorized();
                }));

        return SalesReportHelper.buildCategoryDetailReports(itemsByCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesByProductReport> getSalesBySellerProducts(LocalDateTime start, LocalDateTime end,
                                                                Long establishmentId, List<Long> sellerIds,
                                                                List<Long> productIds) {
        List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, start, end);

        List<Long> userIds = SalesReportHelper.mapEmployeeIdsToUserIds(sellerIds, employeeRepository);
        final List<Long> finalSellerUserIds = userIds;

        Map<Product, List<SaleItem>> itemsByProduct = sales.stream()
                .filter(s -> finalSellerUserIds == null || (s.getUser() != null && finalSellerUserIds.contains(s.getUser().getId())))
                .flatMap(s -> s.getItems().stream())
                .filter(item -> productIds == null || productIds.isEmpty() || productIds.contains(item.getProduct().getId()))
                .collect(Collectors.groupingBy(SaleItem::getProduct));

        return SalesReportHelper.buildProductReports(itemsByProduct, SalesReportHelper::labFromLaboratory);
    }

    // ── Customer-specific reports ──

    @Override
    @Transactional(readOnly = true)
    public List<SalesByCustomerReport> getSalesByCustomer(LocalDateTime start, LocalDateTime end,
                                                           Long establishmentId, List<Long> customerIds) {
        List<Sale> sales = saleRepository.findByFilters(establishmentId, start, end, null, null);

        sales = SalesReportHelper.filterByCustomerIds(sales, customerIds);

        return sales.stream()
                .filter(s -> !s.isVoided())
                .collect(Collectors.groupingBy(s -> s.getCustomer() != null ? s.getCustomer() : SalesReportHelper.createUnknownCustomer()))
                .entrySet().stream()
                .map(entry -> {
                    Customer customer = entry.getKey();
                    List<Sale> customerSales = entry.getValue();
                    BigDecimal totalSpent = customerSales.stream().map(Sale::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long txCount = customerSales.size();
                    LocalDateTime lastPurchase = customerSales.stream().map(Sale::getDate)
                            .max(LocalDateTime::compareTo).orElse(null);
                    return new SalesByCustomerReport(customer.getId(), customer.getName(),
                            customer.getDocumentNumber(), txCount, totalSpent);
                })
                .sorted((a, b) -> Long.compare(b.transactionCount(), a.transactionCount()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesReport> getSalesByCustomerDetail(LocalDateTime start, LocalDateTime end,
                                                       Long establishmentId, List<Long> customerIds) {
        List<Sale> sales = saleRepository.findByFilters(establishmentId, start, end, null, null);

        sales = SalesReportHelper.filterByCustomerIds(sales, customerIds);

        return sales.stream()
                .map(SalesReportHelper::toSalesReport)
                .collect(Collectors.toList());
    }

    // ── Private helpers (delegados a SalesReportHelper) ──

    private Category createUncategorized() {
        return SalesReportHelper.createUncategorized();
    }

    private Laboratory createUnknownLaboratory() {
        return SalesReportHelper.createUnknownLaboratory();
    }

    private Customer createUnknownCustomer() {
        return SalesReportHelper.createUnknownCustomer();
    }
}
