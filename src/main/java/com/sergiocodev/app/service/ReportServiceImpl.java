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

        public ReportServiceImpl(SaleRepository saleRepository, InventoryRepository inventoryRepository) {
                this.saleRepository = saleRepository;
                this.inventoryRepository = inventoryRepository;
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
}
