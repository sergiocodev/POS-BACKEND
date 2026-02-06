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
import java.util.ArrayList;
import java.util.List;
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

        @Override
        @Transactional(readOnly = true)
        public DashboardSummaryResponse getSummaryCards(Long establishmentId) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startToday = now.toLocalDate().atStartOfDay();

                // Today up to current hour/minute
                List<Sale> salesToday = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(startToday)
                                                && s.getDate().isBefore(now))
                                .collect(Collectors.toList());

                // Yesterday up to same hour/minute
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

                // Alerts logic
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

                // Building the structured response
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

        @Override
        @Transactional(readOnly = true)
        public DashboardAlertsResponse getAlerts(Long establishmentId) {
                LocalDate threeMonthsFromNow = LocalDate.now().plusMonths(3);
                LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

                long expiringSoon = inventoryRepository.findAll().stream()
                                .filter(inv -> inv.getEstablishment().getId().equals(establishmentId)
                                                && inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                                && inv.getLot().getExpiryDate() != null
                                                && inv.getLot().getExpiryDate().isBefore(threeMonthsFromNow))
                                .count();

                long lowStock = inventoryRepository.findAll().stream()
                                .filter(inv -> inv.getEstablishment().getId().equals(establishmentId)
                                                && inv.getQuantity().compareTo(new BigDecimal("10")) <= 0)
                                .count();

                long pendingSunat = saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && (s.getSunatStatus() == Sale.SunatStatus.PENDING
                                                                || s.getSunatStatus() == Sale.SunatStatus.REJECTED)
                                                && s.getDate().isBefore(yesterday))
                                .count();

                return new DashboardAlertsResponse(expiringSoon, lowStock, pendingSunat);
        }

        @Override
        @Transactional(readOnly = true)
        public List<PaymentMethodDistribution> getPaymentMethods(LocalDate date, Long establishmentId) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                return saleRepository.findAll().stream()
                                .filter(s -> s.getEstablishment().getId().equals(establishmentId)
                                                && !s.isVoided()
                                                && s.getDate().isAfter(start)
                                                && s.getDate().isBefore(end))
                                .flatMap(s -> s.getPayments().stream())
                                .collect(Collectors.groupingBy(SalePayment::getPaymentMethod,
                                                Collectors.reducing(BigDecimal.ZERO, SalePayment::getAmount,
                                                                BigDecimal::add)))
                                .entrySet().stream()
                                .map(e -> new PaymentMethodDistribution(e.getKey(), e.getValue()))
                                .collect(Collectors.toList());
        }

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
                                .collect(Collectors.groupingBy(item -> item.getProduct(),
                                                Collectors.reducing(BigDecimal.ZERO, item -> item.getQuantity(),
                                                                BigDecimal::add)))
                                .entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .limit(limit)
                                .map(e -> new TopProductDashboard(e.getKey().getId(), e.getKey().getName(),
                                                e.getValue()))
                                .collect(Collectors.toList());
        }

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
                                .collect(Collectors.groupingBy(Sale::getUser,
                                                Collectors.reducing(BigDecimal.ZERO, Sale::getTotal, BigDecimal::add)))
                                .entrySet().stream()
                                .map(e -> new EmployeePerformanceDashboard(e.getKey().getFullName(), e.getValue()))
                                .sorted((e1, e2) -> e2.totalSold().compareTo(e1.totalSold()))
                                .collect(Collectors.toList());
        }
}
