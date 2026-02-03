package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.report.*;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
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

                List<Sale> sales = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(start)
                                                && s.getDate().isBefore(end))
                                .collect(Collectors.toList());

                BigDecimal totalSales = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalTax = sales.stream().map(Sale::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

                return new DailySalesReport(date, (long) sales.size(), totalSales, totalTax, BigDecimal.ZERO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProfitabilityReport> getProfitability(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                List<Sale> sales = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startTime)
                                                && s.getDate().isBefore(endTime))
                                .collect(Collectors.toList());

                Map<Long, List<SaleItem>> itemsByProduct = sales.stream()
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

                List<ProfitabilityReport> reports = new ArrayList<>();
                for (var entry : itemsByProduct.entrySet()) {
                        List<SaleItem> items = entry.getValue();
                        String name = items.get(0).getProduct().getName();
                        BigDecimal qty = items.stream().map(SaleItem::getQuantity).reduce(BigDecimal.ZERO,
                                        BigDecimal::add);
                        BigDecimal revenue = items.stream().map(SaleItem::getAmount).reduce(BigDecimal.ZERO,
                                        BigDecimal::add);

                        // Calculate cost using recorded unitCost
                        BigDecimal cost = items.stream()
                                        .map(item -> (item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO)
                                                        .multiply(item.getQuantity()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal profit = revenue.subtract(cost);
                        BigDecimal margin = revenue.compareTo(BigDecimal.ZERO) > 0
                                        ? profit.multiply(new BigDecimal("100")).divide(revenue, 2,
                                                        RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO;

                        reports.add(new ProfitabilityReport(entry.getKey(), name, qty, revenue, cost, profit, margin));
                }

                return reports;
        }

        @Override
        @Transactional(readOnly = true)
        public List<SunatStatusReport> getSunatStatus(Long establishmentId) {
                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId))
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

                List<SaleItem> items = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startTime)
                                                && s.getDate().isBefore(endTime))
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.toList());

                Map<Long, BigDecimal> productTotals = items.stream()
                                .collect(Collectors.groupingBy(
                                                item -> item.getProduct().getId(),
                                                Collectors.reducing(BigDecimal.ZERO,
                                                                item -> "quantity".equalsIgnoreCase(sortBy)
                                                                                ? item.getQuantity()
                                                                                : item.getAmount(),
                                                                BigDecimal::add)));

                BigDecimal grandTotal = productTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

                List<TopProductReport> sorted = productTotals.entrySet().stream()
                                .map(e -> {
                                        String name = items.stream()
                                                        .filter(i -> i.getProduct().getId().equals(e.getKey()))
                                                        .findFirst().get().getProduct().getName();
                                        BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                                                        ? e.getValue().multiply(new BigDecimal("100"))
                                                                        .divide(grandTotal, 2, RoundingMode.HALF_UP)
                                                        : BigDecimal.ZERO;
                                        return new TopProductReport(e.getKey(), name, e.getValue(), percentage,
                                                        BigDecimal.ZERO);
                                })
                                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                                .collect(Collectors.toList());

                BigDecimal cumulative = BigDecimal.ZERO;
                for (var r : sorted) {
                        cumulative = cumulative.add(r.getPercentage());
                        r.setCumulativePercentage(cumulative);
                }

                return sorted.stream().limit(limit).collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<CategorySalesReport> getSalesByCategory(LocalDate start, LocalDate end, Long establishmentId) {
                LocalDateTime startTime = start.atStartOfDay();
                LocalDateTime endTime = end.atTime(LocalTime.MAX);

                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startTime)
                                                && s.getDate().isBefore(endTime))
                                .flatMap(s -> s.getItems().stream())
                                .collect(Collectors.groupingBy(
                                                item -> item.getProduct().getCategory() != null
                                                                ? item.getProduct().getCategory()
                                                                : new Category(0L, "Uncategorized", true),
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

                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startTime)
                                                && s.getDate().isBefore(endTime))
                                .collect(Collectors.groupingBy(Sale::getUser))
                                .entrySet().stream()
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

                Map<Integer, List<Sale>> byHour = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startTime)
                                                && s.getDate().isBefore(endTime))
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

                // Get all products with stock in establishment
                List<Inventory> inventoryList = inventoryRepository.findAll().stream()
                                .filter(inv -> inv.getEstablishment().getId().equals(establishmentId)
                                                && inv.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                                .collect(Collectors.toList());

                // Find last sale date for each product
                return inventoryList.stream()
                                .collect(Collectors.groupingBy(inv -> inv.getLot().getProduct()))
                                .entrySet().stream()
                                .map(e -> {
                                        Product p = e.getKey();
                                        BigDecimal stock = e.getValue().stream().map(Inventory::getQuantity)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        LocalDateTime lastSale = saleRepository.findAll().stream()
                                                        .filter(s -> !s.isVoided() && s.getEstablishment().getId()
                                                                        .equals(establishmentId))
                                                        .flatMap(s -> s.getItems().stream())
                                                        .filter(item -> item.getProduct().getId().equals(p.getId()))
                                                        .map(item -> item.getSale().getDate())
                                                        .max(LocalDateTime::compareTo)
                                                        .orElse(p.getCreatedAt()); // Use creation date if never sold

                                        return new LowRotationReport(p.getId(), p.getName(), lastSale, stock);
                                })
                                .filter(r -> r.getLastSaleDate().isBefore(threshold))
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<PurchaseReport> getPurchases(LocalDate start, LocalDate end, Long establishmentId) {
                return purchaseRepository.findAll().stream()
                                .filter(p -> p.getEstablishment().getId().equals(establishmentId)
                                                && !p.getIssueDate().isBefore(start)
                                                && !p.getIssueDate().isAfter(end))
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

                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.getDate().isBefore(startTime)
                                                && !s.getDate().isAfter(endTime))
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

                List<Sale> sales = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.getDate().isBefore(startTime)
                                                && !s.getDate().isAfter(endTime))
                                .collect(Collectors.toList());

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
}
