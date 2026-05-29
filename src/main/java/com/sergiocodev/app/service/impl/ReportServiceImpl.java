package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.ReportService;

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
        private final UserRepository userRepository;
        private final EmployeeRepository employeeRepository;
        private final com.sergiocodev.app.repository.AccountPayableRepository accountPayableRepository;
        private final CashSessionRepository cashSessionRepository;
        private final CashMovementRepository cashMovementRepository;

        public ReportServiceImpl(SaleRepository saleRepository, PurchaseRepository purchaseRepository,
                        InventoryRepository inventoryRepository,
                        DocumentSequenceRepository documentSequenceRepository,
                        UserRepository userRepository,
                        EmployeeRepository employeeRepository,
                        com.sergiocodev.app.repository.AccountPayableRepository accountPayableRepository,
                        CashSessionRepository cashSessionRepository,
                        CashMovementRepository cashMovementRepository) {
                this.saleRepository = saleRepository;
                this.purchaseRepository = purchaseRepository;
                this.inventoryRepository = inventoryRepository;
                this.documentSequenceRepository = documentSequenceRepository;
                this.userRepository = userRepository;
                this.employeeRepository = employeeRepository;
                this.accountPayableRepository = accountPayableRepository;
                this.cashSessionRepository = cashSessionRepository;
                this.cashMovementRepository = cashMovementRepository;
        }

        @Override
        @Transactional(readOnly = true)
        public DailySalesReport getDailySales(LocalDateTime date, Long establishmentId) {
                LocalDateTime start = date.toLocalDate().atStartOfDay();
                LocalDateTime end = date.toLocalDate().atTime(LocalTime.MAX);

                // Query at DB level instead of loading all sales
                List<Sale> sales = saleRepository.findByEstablishmentAndDateRangeOrderByDateDesc(
                                establishmentId, start, end, Pageable.unpaged()).getContent();

                BigDecimal totalSales = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalTax = sales.stream().map(Sale::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

                return new DailySalesReport(date.toLocalDate(), (long) sales.size(), totalSales, totalTax, BigDecimal.ZERO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProfitabilityReport> getProfitability(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
                                                inv -> inv.getCostPrice() != null ? inv.getCostPrice()
                                                                : BigDecimal.ZERO,
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

                log.info("Profitability report generated: {} products, establishmentId={}", reports.size(),
                                establishmentId);
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
        public List<TopProductReport> getTopProducts(LocalDateTime start, LocalDateTime end, Long establishmentId,
                        String sortBy, int limit) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
                                        BigDecimal value = "quantity".equalsIgnoreCase(sortBy) ? (BigDecimal) row[1]
                                                        : (BigDecimal) row[2];
                                        BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                                                        ? ((BigDecimal) row[2]).multiply(new BigDecimal("100"))
                                                                        .divide(grandTotal, 2, RoundingMode.HALF_UP)
                                                        : BigDecimal.ZERO;
                                        // Product name needs a lookup - keep it simple with a single query
                                        return new TopProductReport(productId, "Product #" + productId, value,
                                                        percentage,
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
        public List<CategorySalesReport> getSalesByCategory(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
        public List<EmployeeSalesReport> getSalesByEmployee(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
        public List<HourlyHeatReport> getHourlyHeat(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
                                        LocalDateTime lastSale = saleRepository
                                                        .findByEstablishmentAndDateRangeOrderByDateDesc(
                                                                        establishmentId, p.getCreatedAt(),
                                                                        LocalDateTime.now(),
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
        public List<PurchaseReport> getPurchases(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                // Use paginated repository query
                List<Purchase> purchases = purchaseRepository.findByEstablishmentAndDateRangeList(
                                establishmentId, start.toLocalDate(), end.toLocalDate());

                return mapPurchasesToReport(purchases);
        }

        @Override
        @Transactional(readOnly = true)
        public List<PurchaseDebtReport> getPurchaseDebtStatus(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                List<Purchase> purchases = purchaseRepository.findByEstablishmentAndDateRangeList(
                                establishmentId, start.toLocalDate(), end.toLocalDate());

                return purchases.stream().map(p -> {
                        String group = "CANCELADO";
                        BigDecimal pending = BigDecimal.ZERO;
                        LocalDate dueDate = null;
                        Integer overdueDays = 0;
                        Integer creditDays = 0;
                        LocalDate paymentDate = p.getIssueDate(); // default to issue date if CONTADO

                        if (p.getPaymentCondition() == Purchase.PaymentCondition.CREDITO) {
                                // Buscamos la cuenta por pagar
                                com.sergiocodev.app.model.AccountPayable account = accountPayableRepository.findByPurchaseId(p.getId()).orElse(null);
                                if (account != null) {
                                        pending = account.getPendingBalance();
                                        dueDate = account.getDueDate();
                                        if (dueDate != null && dueDate.isAfter(p.getIssueDate())) {
                                                creditDays = (int) java.time.temporal.ChronoUnit.DAYS.between(p.getIssueDate(), dueDate);
                                        }

                                        if (account.getStatus() == com.sergiocodev.app.model.AccountPayable.PayableStatus.PAID) {
                                                group = "CANCELADO";
                                                paymentDate = account.getUpdatedAt().toLocalDate(); // Asumiendo que se pagó ahí
                                        } else {
                                                if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                                                        group = "VENCIDO";
                                                        overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                                                } else {
                                                        group = "CREDITO";
                                                }
                                        }
                                } else {
                                        group = "CREDITO";
                                        pending = p.getTotal();
                                }
                        }

                        return new PurchaseDebtReport(
                                        p.getId(),
                                        p.getIssueDate(),
                                        p.getSupplier() != null ? p.getSupplier().getName() : "Proveedor",
                                        p.getDocumentType() != null ? p.getDocumentType().name() : "Comprobante",
                                        (p.getSeries() != null ? p.getSeries() + "-" : "") + (p.getNumber() != null ? p.getNumber() : ""),
                                        p.getPaymentCondition() != null ? p.getPaymentCondition().name() : "CONTADO",
                                        "Efectivo", // Mock
                                        paymentDate,
                                        creditDays,
                                        dueDate,
                                        overdueDays,
                                        p.getTotal(),
                                        pending,
                                        group
                        );
                }).collect(Collectors.toList());
        }



        @Override
        @Transactional(readOnly = true)
        public List<PurchasesByCategoryDetailReport> getPurchasesByCategoryDetail(LocalDateTime start, LocalDateTime end, Long establishmentId, List<Long> categoryIds) {
                final List<Long> finalCategoryIds = (categoryIds != null && categoryIds.isEmpty()) ? null : categoryIds;
                
                List<Purchase> purchases = purchaseRepository.findByCategoryFilters(
                                establishmentId, start.toLocalDate(), end.toLocalDate(), finalCategoryIds);

                // Group items by category, then sub-group by product
                Map<Category, List<com.sergiocodev.app.model.PurchaseItem>> itemsByCategory = purchases.stream()
                                .filter(p -> !"VOIDED".equals(p.getStatus().name()))
                                .flatMap(p -> p.getItems().stream())
                                .filter(item -> finalCategoryIds == null || (item.getProduct().getCategory() != null && finalCategoryIds.contains(item.getProduct().getCategory().getId())))
                                .collect(Collectors.groupingBy(
                                                item -> {
                                                        Category cat = item.getProduct().getCategory();
                                                        return cat != null ? cat : createUncategorized();
                                                }));

                return itemsByCategory.entrySet().stream()
                                .map(entry -> {
                                        Category cat = entry.getKey();
                                        List<com.sergiocodev.app.model.PurchaseItem> items = entry.getValue();

                                        BigDecimal totalSpent = items.stream()
                                                        .map(com.sergiocodev.app.model.PurchaseItem::getTotalCost)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalQty = items.stream()
                                                        .map(i -> BigDecimal.valueOf(i.getQuantity() != null ? i.getQuantity() : 0))
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        // Sub-group by product
                                        List<PurchasesByCategoryDetailReport.ProductDetail> products = items.stream()
                                                        .collect(Collectors.groupingBy(
                                                                        item -> item.getProduct().getId()))
                                                        .entrySet().stream()
                                                        .map(productEntry -> {
                                                                List<com.sergiocodev.app.model.PurchaseItem> productItems = productEntry.getValue();
                                                                Product product = productItems.get(0).getProduct();
                                                                BigDecimal pQty = productItems.stream()
                                                                                .map(i -> BigDecimal.valueOf(i.getQuantity() != null ? i.getQuantity() : 0))
                                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                                                BigDecimal pSpent = productItems.stream()
                                                                                .map(com.sergiocodev.app.model.PurchaseItem::getTotalCost)
                                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                                                String labName = product.getLaboratory() != null
                                                                                ? product.getLaboratory().getName()
                                                                                : "N/A";
                                                                return new PurchasesByCategoryDetailReport.ProductDetail(
                                                                                product.getId(),
                                                                                product.getTradeName(),
                                                                                labName,
                                                                                pQty, pSpent);
                                                        })
                                                        .sorted((a, b) -> b.spent().compareTo(a.spent()))
                                                        .collect(Collectors.toList());

                                        return new PurchasesByCategoryDetailReport(
                                                        cat.getId(), cat.getName(), totalSpent, totalQty,
                                                        (long) products.size(), products);
                                })
                                .sorted((a, b) -> b.totalSpent().compareTo(a.totalSpent()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<PurchasesBySupplierReport> getPurchasesBySupplier(LocalDateTime start, LocalDateTime end, Long establishmentId, List<Long> supplierIds) {
                final List<Long> finalSupplierIds = (supplierIds != null && supplierIds.isEmpty()) ? null : supplierIds;
                
                List<Purchase> purchases = purchaseRepository.findByFilters(
                                establishmentId, start.toLocalDate(), end.toLocalDate(), finalSupplierIds, null, null, null);

                Map<com.sergiocodev.app.model.Supplier, List<Purchase>> purchasesBySupplier = purchases.stream()
                                .filter(p -> !"VOIDED".equals(p.getStatus().name()))
                                .filter(p -> p.getSupplier() != null)
                                .collect(Collectors.groupingBy(Purchase::getSupplier));

                return purchasesBySupplier.entrySet().stream()
                                .map(entry -> {
                                        com.sergiocodev.app.model.Supplier supplier = entry.getKey();
                                        List<Purchase> supplierPurchases = entry.getValue();

                                        long purchaseCount = supplierPurchases.size();
                                        BigDecimal totalSpent = supplierPurchases.stream()
                                                        .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        
                                        LocalDate lastPurchaseDate = supplierPurchases.stream()
                                                        .map(Purchase::getIssueDate)
                                                        .max(LocalDate::compareTo)
                                                        .orElse(null);

                                        List<PurchasesBySupplierReport.ProductDetail> products = supplierPurchases.stream()
                                                        .flatMap(p -> p.getItems().stream())
                                                        .collect(Collectors.groupingBy(item -> item.getProduct().getId()))
                                                        .entrySet().stream()
                                                        .map(productEntry -> {
                                                                List<com.sergiocodev.app.model.PurchaseItem> productItems = productEntry.getValue();
                                                                Product product = productItems.get(0).getProduct();
                                                                BigDecimal pQty = productItems.stream()
                                                                                .map(i -> BigDecimal.valueOf(i.getQuantity() != null ? i.getQuantity() : 0))
                                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                                                BigDecimal pSpent = productItems.stream()
                                                                                .map(com.sergiocodev.app.model.PurchaseItem::getTotalCost)
                                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                                                BigDecimal unitPrice = pQty.compareTo(BigDecimal.ZERO) > 0 ? pSpent.divide(pQty, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                                                                String labName = product.getLaboratory() != null ? product.getLaboratory().getName() : "N/A";
                                                                return new PurchasesBySupplierReport.ProductDetail(
                                                                                product.getTradeName(),
                                                                                labName,
                                                                                pQty,
                                                                                unitPrice,
                                                                                pSpent);
                                                        })
                                                        .sorted((a, b) -> b.total().compareTo(a.total()))
                                                        .collect(Collectors.toList());

                                        return new PurchasesBySupplierReport(
                                                        supplier.getId(),
                                                        supplier.getName(),
                                                        purchaseCount,
                                                        totalSpent,
                                                        lastPurchaseDate,
                                                        supplier.getStatus() != null ? supplier.getStatus().name() : "N/A",
                                                        products);
                                })
                                .sorted((a, b) -> b.totalSpent().compareTo(a.totalSpent()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<AccountsPayableSupplierReport> getAccountsPayableBySupplier(LocalDateTime start, LocalDateTime end, Long establishmentId, List<Long> supplierIds) {
                final List<Long> finalSupplierIds = (supplierIds != null && supplierIds.isEmpty()) ? null : supplierIds;
                
                List<AccountPayable> allPayables = accountPayableRepository.findAll((root, query, cb) -> {
                        List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
                        
                        jakarta.persistence.criteria.Join<AccountPayable, Purchase> purchaseJoin = root.join("purchase");
                        
                        predicates.add(cb.equal(purchaseJoin.get("establishment").get("id"), establishmentId));
                        predicates.add(cb.between(purchaseJoin.get("issueDate"), start.toLocalDate(), end.toLocalDate()));
                        
                        if (finalSupplierIds != null && !finalSupplierIds.isEmpty()) {
                                predicates.add(root.get("supplier").get("id").in(finalSupplierIds));
                        }
                        
                        predicates.add(root.get("status").in(AccountPayable.PayableStatus.PENDING, AccountPayable.PayableStatus.PARTIAL));
                        
                        return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                });

                Map<com.sergiocodev.app.model.Supplier, List<AccountPayable>> payablesBySupplier = allPayables.stream()
                                .filter(ap -> ap.getSupplier() != null)
                                .collect(Collectors.groupingBy(AccountPayable::getSupplier));

                return payablesBySupplier.entrySet().stream()
                                .map(entry -> {
                                        com.sergiocodev.app.model.Supplier supplier = entry.getKey();
                                        List<AccountPayable> supplierPayables = entry.getValue();

                                        int pendingInvoicesCount = supplierPayables.size();
                                        
                                        BigDecimal totalPending = supplierPayables.stream()
                                                        .map(ap -> ap.getPendingBalance() != null ? ap.getPendingBalance() : BigDecimal.ZERO)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        
                                        BigDecimal overdueDebt = supplierPayables.stream()
                                                        .filter(ap -> ap.getDueDate() != null && ap.getDueDate().isBefore(LocalDate.now()))
                                                        .map(ap -> ap.getPendingBalance() != null ? ap.getPendingBalance() : BigDecimal.ZERO)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        List<AccountsPayableSupplierReport.InvoiceDetail> invoices = supplierPayables.stream()
                                                        .map(ap -> {
                                                                Purchase p = ap.getPurchase();
                                                                String invoiceNumber = (p.getSeries() != null ? p.getSeries() + "-" : "") + (p.getNumber() != null ? p.getNumber() : "");
                                                                BigDecimal pendingAmt = ap.getPendingBalance() != null ? ap.getPendingBalance() : BigDecimal.ZERO;
                                                                String status = "Por vencer";
                                                                if (ap.getDueDate() != null) {
                                                                        if (ap.getDueDate().isBefore(LocalDate.now())) {
                                                                                status = "Vencido";
                                                                        } else if (ap.getDueDate().isBefore(LocalDate.now().plusDays(5))) {
                                                                                status = "Próximo vencer";
                                                                        }
                                                                }
                                                                return new AccountsPayableSupplierReport.InvoiceDetail(
                                                                                invoiceNumber,
                                                                                p.getIssueDate(),
                                                                                ap.getDueDate(),
                                                                                pendingAmt,
                                                                                status);
                                                        })
                                                        .sorted((a, b) -> {
                                                                if (a.dueDate() == null && b.dueDate() == null) return 0;
                                                                if (a.dueDate() == null) return 1;
                                                                if (b.dueDate() == null) return -1;
                                                                return a.dueDate().compareTo(b.dueDate());
                                                        })
                                                        .collect(Collectors.toList());

                                        return new AccountsPayableSupplierReport(
                                                        supplier.getId(),
                                                        supplier.getName(),
                                                        totalPending,
                                                        pendingInvoicesCount,
                                                        overdueDebt,
                                                        invoices);
                                })
                                .sorted((a, b) -> b.totalPending().compareTo(a.totalPending()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProductPriceHistoryReport> getProductPriceHistory(LocalDateTime start, LocalDateTime end, Long establishmentId, Long productId) {
                List<Purchase> purchases = purchaseRepository.findByProductFilters(
                                establishmentId, start.toLocalDate(), end.toLocalDate(), productId);

                List<com.sergiocodev.app.model.PurchaseItem> allItems = purchases.stream()
                                .filter(p -> !"VOIDED".equals(p.getStatus().name()))
                                .flatMap(p -> p.getItems().stream())
                                .filter(item -> productId == null || item.getProduct().getId().equals(productId))
                                .collect(Collectors.toList());

                Map<Product, List<com.sergiocodev.app.model.PurchaseItem>> itemsByProduct = allItems.stream()
                                .collect(Collectors.groupingBy(com.sergiocodev.app.model.PurchaseItem::getProduct));

                return itemsByProduct.entrySet().stream()
                                .map(entry -> {
                                        Product product = entry.getKey();
                                        List<com.sergiocodev.app.model.PurchaseItem> productItems = entry.getValue();

                                        productItems.sort(java.util.Comparator.comparing(i -> i.getPurchase().getIssueDate()));

                                        List<ProductPriceHistoryReport.PriceHistoryDetail> history = new java.util.ArrayList<>();
                                        BigDecimal lowestPrice = null;
                                        BigDecimal highestPrice = null;
                                        BigDecimal currentPrice = BigDecimal.ZERO;
                                        BigDecimal previousPrice = null;

                                        for (com.sergiocodev.app.model.PurchaseItem item : productItems) {
                                                BigDecimal qty = item.getQuantity() != null ? BigDecimal.valueOf(item.getQuantity()) : BigDecimal.ZERO;
                                                BigDecimal totalCost = item.getTotalCost() != null ? item.getTotalCost() : BigDecimal.ZERO;
                                                BigDecimal unitPrice = qty.compareTo(BigDecimal.ZERO) > 0 ? totalCost.divide(qty, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

                                                if (lowestPrice == null || unitPrice.compareTo(lowestPrice) < 0) lowestPrice = unitPrice;
                                                if (highestPrice == null || unitPrice.compareTo(highestPrice) > 0) highestPrice = unitPrice;
                                                currentPrice = unitPrice;

                                                String variation = "—";
                                                if (previousPrice != null && previousPrice.compareTo(BigDecimal.ZERO) > 0) {
                                                        BigDecimal diff = unitPrice.subtract(previousPrice);
                                                        BigDecimal percent = diff.divide(previousPrice, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                                                        if (percent.compareTo(BigDecimal.ZERO) > 0) {
                                                                variation = "+" + percent.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "%";
                                                        } else if (percent.compareTo(BigDecimal.ZERO) < 0) {
                                                                variation = percent.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "%";
                                                        } else {
                                                                variation = "0%";
                                                        }
                                                }

                                                history.add(new ProductPriceHistoryReport.PriceHistoryDetail(
                                                                item.getPurchase().getIssueDate(),
                                                                product.getTradeName(),
                                                                item.getPurchase().getSupplier() != null ? item.getPurchase().getSupplier().getName() : "N/A",
                                                                qty,
                                                                unitPrice,
                                                                variation
                                                ));
                                                
                                                previousPrice = unitPrice;
                                        }

                                        history.sort((a, b) -> b.date().compareTo(a.date()));

                                        String totalVariation = "—";
                                        if (history.size() > 1) {
                                                BigDecimal firstQty = productItems.get(0).getQuantity() != null ? BigDecimal.valueOf(productItems.get(0).getQuantity()) : BigDecimal.ZERO;
                                                BigDecimal firstTotal = productItems.get(0).getTotalCost() != null ? productItems.get(0).getTotalCost() : BigDecimal.ZERO;
                                                BigDecimal firstPrice = firstQty.compareTo(BigDecimal.ZERO) > 0 ? firstTotal.divide(firstQty, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                                                
                                                if (firstPrice.compareTo(BigDecimal.ZERO) > 0) {
                                                        BigDecimal diff = currentPrice.subtract(firstPrice);
                                                        BigDecimal percent = diff.divide(firstPrice, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                                                        if (percent.compareTo(BigDecimal.ZERO) > 0) {
                                                                totalVariation = "+" + percent.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "%";
                                                        } else if (percent.compareTo(BigDecimal.ZERO) < 0) {
                                                                totalVariation = percent.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "%";
                                                        } else {
                                                                totalVariation = "0%";
                                                        }
                                                }
                                        }

                                        return new ProductPriceHistoryReport(
                                                        product.getId(),
                                                        product.getTradeName(),
                                                        currentPrice,
                                                        lowestPrice != null ? lowestPrice : BigDecimal.ZERO,
                                                        highestPrice != null ? highestPrice : BigDecimal.ZERO,
                                                        totalVariation,
                                                        history
                                        );
                                })
                                .sorted(java.util.Comparator.comparing(ProductPriceHistoryReport::productName))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<PurchasesByBuyerReport> getPurchasesByBuyer(LocalDateTime start, LocalDateTime end, Long establishmentId, List<Long> buyerIds) {
                final List<Long> finalBuyerIds = (buyerIds != null && buyerIds.isEmpty()) ? null : buyerIds;
                
                List<Purchase> purchases = purchaseRepository.findByFilters(
                                establishmentId, start.toLocalDate(), end.toLocalDate(), null, null, finalBuyerIds, null);

                Map<com.sergiocodev.app.model.User, List<Purchase>> purchasesByUser = purchases.stream()
                                .filter(p -> p.getUser() != null)
                                .collect(Collectors.groupingBy(Purchase::getUser));

                return purchasesByUser.entrySet().stream()
                                .map(entry -> {
                                        com.sergiocodev.app.model.User user = entry.getKey();
                                        List<Purchase> userPurchases = entry.getValue();

                                        int totalPurchases = userPurchases.size();
                                        
                                        BigDecimal totalSpent = userPurchases.stream()
                                                        .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        
                                        BigDecimal avgPurchase = totalPurchases > 0 ? totalSpent.divide(BigDecimal.valueOf(totalPurchases), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                                        
                                        LocalDate lastPurchaseDate = userPurchases.stream()
                                                        .map(Purchase::getIssueDate)
                                                        .filter(java.util.Objects::nonNull)
                                                        .max(LocalDate::compareTo)
                                                        .orElse(null);

                                        List<PurchasesByBuyerReport.PurchaseDetail> details = userPurchases.stream()
                                                        .map(p -> {
                                                                String document = (p.getSeries() != null ? p.getSeries() + "-" : "") + (p.getNumber() != null ? p.getNumber() : "");
                                                                String supplierName = p.getSupplier() != null ? p.getSupplier().getName() : "N/A";
                                                                
                                                                return new PurchasesByBuyerReport.PurchaseDetail(
                                                                                p.getIssueDate(),
                                                                                user.getFullName(),
                                                                                supplierName,
                                                                                document,
                                                                                p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO
                                                                );
                                                        })
                                                        .sorted((a, b) -> {
                                                                if (a.date() == null && b.date() == null) return 0;
                                                                if (a.date() == null) return 1;
                                                                if (b.date() == null) return -1;
                                                                return b.date().compareTo(a.date());
                                                        })
                                                        .collect(Collectors.toList());

                                        return new PurchasesByBuyerReport(
                                                        user.getId(),
                                                        user.getFullName(),
                                                        totalPurchases,
                                                        totalSpent,
                                                        lastPurchaseDate,
                                                        avgPurchase,
                                                        details
                                        );
                                })
                                .sorted((a, b) -> b.totalSpent().compareTo(a.totalSpent()))
                                .collect(Collectors.toList());
        }


        private List<PurchaseReport> mapPurchasesToReport(List<Purchase> purchases) {
                return purchases.stream()
                                .map(p -> new PurchaseReport(
                                                p.getId(),
                                                p.getSupplier() != null ? p.getSupplier().getName() : "Proveedor General",
                                                p.getDocumentType() != null ? p.getDocumentType().name() : "N/A",
                                                (p.getSeries() != null ? p.getSeries() + "-" : "") +
                                                                (p.getNumber() != null ? p.getNumber() : ""),
                                                p.getIssueDate(),
                                                p.getArrivalDate(),
                                                p.getSubTotal(),
                                                p.getTax(),
                                                p.getTotal(),
                                                p.getStatus() != null ? p.getStatus().name() : "N/A",
                                                p.getItems() != null ? p.getItems().size() : 0))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesReport> getSales(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
        public SalesSummaryReport getSalesSummary(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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

                return new SalesSummaryReport(start.toLocalDate(), end.toLocalDate(), totalTransactions, totalRevenue, totalTax,
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
        public List<SalesReport> getSalesFiltered(LocalDateTime start, LocalDateTime end, Long establishmentId,
                        Sale.SaleDocumentType documentType, String series, Long sellerId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

                List<Sale> sales = saleRepository.findByFilters(establishmentId, startTime, endTime,
                                documentType, series);

                if (sellerId != null) {
                        sales = sales.stream().filter(s -> s.getUser() != null && s.getUser().getId().equals(sellerId)).collect(Collectors.toList());
                }

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
        public List<SalesBySeriesReport> getSalesBySeries(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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

                                        Long initialNum = group.stream().map(Sale::getNumber)
                                                        .map(Long::valueOf)
                                                        .min(Long::compare).orElse(0L);
                                        Long actualNum = group.stream().map(Sale::getNumber)
                                                        .map(Long::valueOf)
                                                        .max(Long::compare).orElse(0L);

                                        return new SalesBySeriesReport(docType, seriesVal,
                                                        initialNum, actualNum,
                                                        (long) valid.size(), totalSubTotal, totalTax,
                                                        totalAmount, (long) voided.size(), voidedAmount);
                                })
                                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByPaymentMethodReport> getSalesByPaymentMethod(LocalDateTime start, LocalDateTime end,
                        Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
        public List<SalesByLaboratoryReport> getSalesByLaboratory(LocalDateTime start, LocalDateTime end,
                        Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
        public List<SalesByEmployeeCategoryReport> getSalesByEmployeeCategory(LocalDateTime start, LocalDateTime end,
                        Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
        public List<SalesByCategoryDetailReport> getSalesByCategoryDetail(LocalDateTime start, LocalDateTime end,
                        Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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
        public List<SalesByProductReport> getSalesByProduct(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

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

                                        String catName = product.getCategory() != null ? product.getCategory().getName()
                                                        : "Sin Categoría";
                                        String labName = product.getLaboratory() != null
                                                        ? product.getLaboratory().getName()
                                                        : "Sin Laboratorio";
                                        String therapeuticAction = product.getTherapeuticActions().stream()
                                                        .map(TherapeuticAction::getName)
                                                        .collect(Collectors.joining(", "));

                                        return new SalesByProductReport(
                                                        product.getId(),
                                                        product.getTradeName(),
                                                        catName,
                                                        labName,
                                                        therapeuticAction,
                                                        quantitySold,
                                                        totalRevenue);
                                })
                                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<String> getAvailableSeries(Long establishmentId, Sale.SaleDocumentType documentType) {
                if (documentType == null) {
                        return documentSequenceRepository.findSeriesByEstablishment(establishmentId);
                }

                DocumentSequence.DocumentType seqDocType;
                try {
                        seqDocType = DocumentSequence.DocumentType.valueOf(documentType.name());
                } catch (Exception e) {
                        return List.of();
                }
                return documentSequenceRepository.findSeriesByEstablishmentAndDocumentType(establishmentId, seqDocType);
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByCategoryDetailReport> getSalesByCategories(LocalDateTime start, LocalDateTime end,
                        Long establishmentId, List<Long> categoryIds, Long sellerId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                if (sellerId != null) {
                        sales = sales.stream().filter(s -> s.getUser() != null && s.getUser().getId().equals(sellerId)).collect(Collectors.toList());
                }

                Map<Category, List<SaleItem>> itemsByCategory = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .filter(item -> {
                                        Category cat = item.getProduct().getCategory();
                                        if (categoryIds == null || categoryIds.isEmpty())
                                                return true;
                                        return cat != null && categoryIds.contains(cat.getId());
                                })
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

        @Override
        @Transactional(readOnly = true)
        public List<SalesByProductReport> getSalesByProductFilters(LocalDateTime start, LocalDateTime end, Long establishmentId,
                        List<Long> productIds, List<Long> brandIds, List<Long> therapeuticActionIds, Long sellerId) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                if (sellerId != null) {
                        sales = sales.stream().filter(s -> s.getUser() != null && s.getUser().getId().equals(sellerId)).collect(Collectors.toList());
                }

                Map<Product, List<SaleItem>> itemsByProduct = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .filter(item -> {
                                        Product p = item.getProduct();
                                        boolean matchProduct = productIds == null || productIds.isEmpty()
                                                        || productIds.contains(p.getId());
                                        boolean matchBrand = brandIds == null || brandIds.isEmpty()
                                                        || (p.getBrand() != null
                                                                        && brandIds.contains(
                                                                                        p.getBrand().getId()));
                                        boolean matchTherapeutic = therapeuticActionIds == null
                                                        || therapeuticActionIds.isEmpty()
                                                        || p.getTherapeuticActions().stream()
                                                                        .anyMatch(ta -> therapeuticActionIds
                                                                                        .contains(ta.getId()));

                                        return matchProduct && matchBrand && matchTherapeutic;
                                })
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

                                        String catName = product.getCategory() != null ? product.getCategory().getName()
                                                        : "Sin Categoría";
                                        String labName = product.getBrand() != null
                                                        ? product.getBrand().getName()
                                                        : "Sin Marca";
                                        String therapeuticAction = product.getTherapeuticActions().stream()
                                                        .map(TherapeuticAction::getName)
                                                        .collect(Collectors.joining(", "));

                                        return new SalesByProductReport(
                                                        product.getId(),
                                                        product.getTradeName(),
                                                        catName,
                                                        labName,
                                                        therapeuticAction,
                                                        quantitySold,
                                                        totalRevenue);
                                })
                                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesBySeriesReport> getSalesBySeriesFiltered(LocalDateTime start, LocalDateTime end, Long establishmentId,
                        List<String> seriesList) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

                List<Sale> sales = saleRepository.findAllForReport(establishmentId, startTime, endTime,
                                Pageable.unpaged()).getContent();

                return sales.stream()
                                .filter(s -> seriesList == null || seriesList.isEmpty()
                                                || seriesList.contains(s.getSeries()))
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

                                        Long initialNum = group.stream().map(Sale::getNumber)
                                                        .map(Long::valueOf)
                                                        .min(Long::compare).orElse(0L);
                                        Long actualNum = group.stream().map(Sale::getNumber)
                                                        .map(Long::valueOf)
                                                        .max(Long::compare).orElse(0L);

                                        return new SalesBySeriesReport(docType, seriesVal,
                                                        initialNum, actualNum,
                                                        (long) valid.size(), totalSubTotal, totalTax,
                                                        totalAmount, (long) voided.size(), voidedAmount);
                                })
                                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                                .collect(Collectors.toList());
        }

        // ── Reportes por Vendedor ──

        @Override
        @Transactional(readOnly = true)
        public List<SalesReport> getSalesBySeller(LocalDateTime start, LocalDateTime end, Long establishmentId,
                        List<Long> sellerIds) {
                // Map Employee IDs (from frontend) to User IDs (stored in Sales)
                List<Long> userIds = null;
                if (sellerIds != null && !sellerIds.isEmpty() && !sellerIds.contains(0L)) {
                        userIds = employeeRepository.findAllById(sellerIds).stream()
                                        .filter(e -> e.getUser() != null)
                                        .map(e -> e.getUser().getId())
                                        .collect(Collectors.toList());

                        if (userIds.isEmpty()) {
                                return new ArrayList<>(); // Selected employees have no associated users
                        }
                }

                List<Sale> sales = saleRepository.findByFilters(establishmentId, start, end, null, null);

                // Filter by user IDs if not "Todos"
                if (userIds != null) {
                        final List<Long> finalUserIds = userIds;
                        sales = sales.stream()
                                        .filter(s -> s.getUser() != null && finalUserIds.contains(s.getUser().getId()))
                                        .collect(Collectors.toList());
                }

                return sales.stream()
                                .map(s -> {
                                        // Get employee name from User -> Employee link if possible
                                        String employeeName = "N/A";
                                        if (s.getUser() != null) {
                                                Employee emp = employeeRepository.findByUserId(s.getUser().getId()).orElse(null);
                                                if (emp != null) {
                                                        employeeName = emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "");
                                                } else {
                                                        employeeName = s.getUser().getFullName();
                                                }
                                        }

                                        return new SalesReport(
                                                        s.getId(),
                                                        s.getCustomer() != null ? s.getCustomer().getName() : "PUBLICO EN GENERAL",
                                                        employeeName,
                                                        s.getDocumentType().name(),
                                                        s.getSeries() + "-" + s.getNumber(),
                                                        s.getDate(),
                                                        s.getSubTotal(),
                                                        s.getTax(),
                                                        s.getTotal(),
                                                        s.getStatus().name(),
                                                        s.getSunatStatus() != null ? s.getSunatStatus().name() : null,
                                                        s.isVoided());
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesByCategoryDetailReport> getSalesBySellerCategories(LocalDateTime start, LocalDateTime end,
                        Long establishmentId, List<Long> sellerIds, List<Long> categoryIds) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                // Filter by seller if not "Todos"
                if (sellerIds != null && !sellerIds.isEmpty() && !sellerIds.contains(0L)) {
                        sales = sales.stream()
                                        .filter(s -> s.getUser() != null && sellerIds.contains(s.getUser().getId()))
                                        .collect(Collectors.toList());
                }

                Map<Category, List<SaleItem>> itemsByCategory = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .filter(item -> {
                                        Category cat = item.getProduct().getCategory();
                                        if (categoryIds == null || categoryIds.isEmpty())
                                                return true;
                                        return cat != null && categoryIds.contains(cat.getId());
                                })
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

                                        List<SalesByCategoryDetailReport.ProductDetail> products = items.stream()
                                                        .collect(Collectors.groupingBy(
                                                                        item -> item.getProduct().getId()))
                                                        .entrySet().stream()
                                                        .map(productEntry -> {
                                                                List<SaleItem> productItems = productEntry.getValue();
                                                                Product product = productItems.get(0).getProduct();
                                                                BigDecimal pQty = productItems.stream()
                                                                                .map(SaleItem::getQuantity)
                                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                                                BigDecimal pRevenue = productItems.stream()
                                                                                .map(SaleItem::getAmount)
                                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        @Override
        @Transactional(readOnly = true)
        public List<SalesByProductReport> getSalesBySellerProducts(LocalDateTime start, LocalDateTime end,
                        Long establishmentId, List<Long> sellerIds, List<Long> productIds) {
                LocalDateTime startTime = start;
                LocalDateTime endTime = end;

                List<Sale> sales = saleRepository.findForCategoryDetailAnalysis(establishmentId, startTime, endTime);

                // Filter by seller if not "Todos"
                if (sellerIds != null && !sellerIds.isEmpty() && !sellerIds.contains(0L)) {
                        sales = sales.stream()
                                        .filter(s -> s.getUser() != null && sellerIds.contains(s.getUser().getId()))
                                        .collect(Collectors.toList());
                }

                Map<Product, List<SaleItem>> itemsByProduct = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .filter(item -> {
                                        if (productIds == null || productIds.isEmpty())
                                                return true;
                                        return productIds.contains(item.getProduct().getId());
                                })
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

                                        String catName = product.getCategory() != null ? product.getCategory().getName()
                                                        : "Sin Categoría";
                                        String labName = product.getLaboratory() != null
                                                        ? product.getLaboratory().getName()
                                                        : "Sin Laboratorio";
                                        String therapeuticAction = product.getTherapeuticActions().stream()
                                                        .map(TherapeuticAction::getName)
                                                        .collect(Collectors.joining(", "));

                                        return new SalesByProductReport(
                                                        product.getId(),
                                                        product.getTradeName(),
                                                        catName,
                                                        labName,
                                                        therapeuticAction,
                                                        quantitySold,
                                                        totalRevenue);
                                })
                                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public String getSellerNames(List<Long> sellerIds) {
                if (sellerIds == null || sellerIds.isEmpty() || sellerIds.contains(0L)) {
                        return "TODOS LOS VENDEDORES";
                }

                if (sellerIds.size() == 1) {
                        return employeeRepository.findById(sellerIds.get(0))
                                        .map(e -> e.getFirstName() + " " + (e.getLastName() != null ? e.getLastName() : ""))
                                        .orElse("Vendedor Desconocido");
                }

                return "VARIOS VENDEDORES (" + sellerIds.size() + ")";
        }

        // ── Reportes por Cliente ──

        @Override
        @Transactional(readOnly = true)
        public List<SalesByCustomerReport> getSalesByCustomer(LocalDateTime start, LocalDateTime end,
                        Long establishmentId, List<Long> customerIds) {
                List<Sale> sales = saleRepository
                                .findByEstablishmentAndDateRangeOrderByDateDesc(establishmentId, start, end, Pageable.unpaged())
                                .getContent().stream()
                                .filter(s -> !s.isVoided())
                                .collect(Collectors.toList());

                // Filter by customer IDs if specified (0 = Todos)
                if (customerIds != null && !customerIds.isEmpty() && !customerIds.contains(0L)) {
                        sales = sales.stream()
                                        .filter(s -> s.getCustomer() != null && customerIds.contains(s.getCustomer().getId()))
                                        .collect(Collectors.toList());
                }

                // Group by customer
                Map<Long, List<Sale>> salesByCustomer = sales.stream()
                                .collect(Collectors.groupingBy(s -> s.getCustomer() != null ? s.getCustomer().getId() : 0L));

                return salesByCustomer.entrySet().stream()
                                .map(entry -> {
                                        List<Sale> customerSales = entry.getValue();
                                        Sale firstSale = customerSales.get(0);
                                        String customerName = firstSale.getCustomer() != null
                                                        ? firstSale.getCustomer().getName()
                                                        : "PUBLICO EN GENERAL";
                                        String documentNumber = firstSale.getCustomer() != null
                                                        ? firstSale.getCustomer().getDocumentNumber()
                                                        : "-";
                                        BigDecimal total = customerSales.stream()
                                                        .map(Sale::getTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        return new SalesByCustomerReport(
                                                        entry.getKey(),
                                                        customerName,
                                                        documentNumber,
                                                        customerSales.size(),
                                                        total);
                                })
                                .sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<SalesReport> getSalesByCustomerDetail(LocalDateTime start, LocalDateTime end,
                        Long establishmentId, List<Long> customerIds) {
                List<Sale> sales = saleRepository
                                .findByEstablishmentAndDateRangeOrderByDateDesc(establishmentId, start, end, Pageable.unpaged())
                                .getContent().stream()
                                .filter(s -> !s.isVoided())
                                .collect(Collectors.toList());

                // Filter by customer IDs if specified (0 = Todos)
                if (customerIds != null && !customerIds.isEmpty() && !customerIds.contains(0L)) {
                        sales = sales.stream()
                                        .filter(s -> s.getCustomer() != null && customerIds.contains(s.getCustomer().getId()))
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

                                        return new SalesReport(
                                                        s.getId(),
                                                        s.getCustomer() != null ? s.getCustomer().getName() : "PUBLICO EN GENERAL",
                                                        employeeName,
                                                        s.getDocumentType().name(),
                                                        s.getSeries() + "-" + s.getNumber(),
                                                        s.getDate(),
                                                        s.getSubTotal(),
                                                        s.getTax(),
                                                        s.getTotal(),
                                                        s.getStatus().name(),
                                                        s.getSunatStatus() != null ? s.getSunatStatus().name() : null,
                                                        s.isVoided());
                                })
                                .collect(Collectors.toList());
        }

        // ── Reportes de Caja ──

        @Override
        @Transactional(readOnly = true)
        public List<CashSessionReport> getCashSessions(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                return cashSessionRepository.findByEstablishmentAndDateRange(establishmentId, start, end)
                                .stream()
                                .map(s -> new CashSessionReport(
                                                s.getId(),
                                                s.getCashRegister() != null ? s.getCashRegister().getName() : "N/A",
                                                s.getUser() != null ? s.getUser().getUsername() : "Unknown",
                                                s.getOpeningBalance(),
                                                s.getClosingBalance(),
                                                s.getCalculatedBalance(),
                                                s.getDiffAmount(),
                                                s.getOpenedAt(),
                                                s.getClosedAt(),
                                                s.getStatus().name()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<CashMovementReport> getCashMovementsBySession(Long sessionId) {
                return cashMovementRepository.findByCashSessionId(sessionId)
                                .stream()
                                .map(m -> new CashMovementReport(
                                                m.getId(),
                                                m.getCashConcept() != null ? m.getCashConcept().getName() : "N/A",
                                                m.getCashConcept() != null && m.getCashConcept().getType() != null
                                                                ? (m.getCashConcept().getType().name().equals("IN") ? "INGRESO" : "EGRESO")
                                                                : "N/A",
                                                m.getAmount(),
                                                m.getReference(),
                                                m.getDescription(),
                                                m.getUser() != null ? m.getUser().getUsername() : "Unknown",
                                                m.getCreatedAt()))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<CashMovementReport> getCashMovementsByPeriod(LocalDateTime start, LocalDateTime end, Long establishmentId) {
                List<com.sergiocodev.app.model.CashSession> sessions =
                                cashSessionRepository.findByEstablishmentAndDateRange(establishmentId, start, end);
                return sessions.stream()
                                .flatMap(s -> cashMovementRepository.findByCashSessionId(s.getId()).stream())
                                .map(m -> new CashMovementReport(
                                                m.getId(),
                                                m.getCashConcept() != null ? m.getCashConcept().getName() : "N/A",
                                                m.getCashConcept() != null && m.getCashConcept().getType() != null
                                                                ? (m.getCashConcept().getType().name().equals("IN") ? "INGRESO" : "EGRESO")
                                                                : "N/A",
                                                m.getAmount(),
                                                m.getReference(),
                                                m.getDescription(),
                                                m.getUser() != null ? m.getUser().getUsername() : "Unknown",
                                                m.getCreatedAt()))
                                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                                .collect(Collectors.toList());
        }
}
