package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.report.DailySalesReport;
import com.sergiocodev.app.dto.report.ProfitabilityReport;
import com.sergiocodev.app.dto.report.SunatStatusReport;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.SaleItem;
import com.sergiocodev.app.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleRepository saleRepository;

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
            BigDecimal qty = items.stream().map(SaleItem::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal revenue = items.stream().map(SaleItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate cost using recorded unitCost
            BigDecimal cost = items.stream()
                    .map(item -> (item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO)
                            .multiply(item.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal profit = revenue.subtract(cost);
            BigDecimal margin = revenue.compareTo(BigDecimal.ZERO) > 0
                    ? profit.multiply(new BigDecimal("100")).divide(revenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            reports.add(new ProfitabilityReport(entry.getKey(), name, qty, revenue, cost, profit, margin));
        }

        return reports;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SunatStatusReport> getSunatStatus(Long establishmentId) {
        List<Sale> sales = saleRepository.findAll().stream()
                .filter(s -> s.getEstablishment().getId().equals(establishmentId))
                .collect(Collectors.toList());

        Map<Sale.SunatStatus, Long> counts = sales.stream()
                .collect(Collectors.groupingBy(Sale::getSunatStatus, Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> new SunatStatusReport(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
