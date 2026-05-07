package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.customer.CustomerDashboardResponse;
import com.sergiocodev.app.dto.customer.CustomerDashboardResponse.*;
import com.sergiocodev.app.dto.customer.CustomerRequest;
import com.sergiocodev.app.dto.customer.CustomerResponse;
import com.sergiocodev.app.exception.CustomerNotFoundException;
import com.sergiocodev.app.exception.DuplicateDocumentException;
import com.sergiocodev.app.mapper.CustomerMapper;
import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

        private final CustomerRepository customerRepository;
        private final CustomerMapper customerMapper;

        private static final String[] AVATAR_COLORS = {
                        "#10b981", "#3b82f6", "#8b5cf6", "#f59e0b",
                        "#ec4899", "#14b8a6", "#f87171", "#6366f1"
        };

        @Override
        @Transactional
        public CustomerResponse create(CustomerRequest request) {
                if (customerRepository.existsByDocumentNumber(request.documentNumber())) {
                        throw new DuplicateDocumentException(
                                        "Document number '" + request.documentNumber() + "' already exists");
                }

                Customer customer = customerMapper.toEntity(request);
                Customer savedCustomer = customerRepository.save(customer);
                return customerMapper.toResponse(savedCustomer);
        }

        @Override
        @Transactional(readOnly = true)
        public List<CustomerResponse> getAll() {
                return customerRepository.findAll()
                                .stream()
                                .map(customerMapper::toResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<CustomerResponse> getAllPaged(String name, String documentNumber, String email, String phone,
                        Pageable pageable) {
                Specification<Customer> spec = (root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        if (name != null && !name.isBlank()) {
                                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                        }
                        if (documentNumber != null && !documentNumber.isBlank()) {
                                predicates.add(cb.like(root.get("documentNumber"), "%" + documentNumber + "%"));
                        }
                        if (email != null && !email.isBlank()) {
                                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
                        }
                        if (phone != null && !phone.isBlank()) {
                                predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
                        }

                        return cb.and(predicates.toArray(new Predicate[0]));
                };

                return customerRepository.findAll(spec, pageable)
                                .map(customerMapper::toResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public CustomerResponse getById(Long id) {
                Customer customer = customerRepository.findById(id)
                                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
                return customerMapper.toResponse(customer);
        }

        @Override
        @Transactional
        public CustomerResponse update(Long id, CustomerRequest request) {
                Customer customer = customerRepository.findById(id)
                                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

                if (!request.documentNumber().equals(customer.getDocumentNumber()) &&
                                customerRepository.existsByDocumentNumber(request.documentNumber())) {
                        throw new DuplicateDocumentException(
                                        "Document number '" + request.documentNumber() + "' already exists");
                }

                customerMapper.updateEntity(request, customer);
                Customer updatedCustomer = customerRepository.save(customer);
                return customerMapper.toResponse(updatedCustomer);
        }

        @Override
        @Transactional
        public void delete(Long id) {
                if (!customerRepository.existsById(id)) {
                        throw new CustomerNotFoundException("Customer not found with ID: " + id);
                }
                customerRepository.deleteById(id);
        }

        @Override
        @Transactional(readOnly = true)
        public CustomerResponse findByDocumentNumber(String documentNumber) {
                Customer customer = customerRepository.findByDocumentNumber(documentNumber)
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with document number: " + documentNumber));
                return customerMapper.toResponse(customer);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Dashboard
        // ─────────────────────────────────────────────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        public CustomerDashboardResponse getDashboard() {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime prevMonthStart = monthStart.minusMonths(1);
                LocalDateTime prevMonthEnd = monthStart.minusSeconds(1);

                // ── KPIs actuales ───────────────────────────────────────────────
                long totalCustomers = customerRepository.count();
                long prevTotal = totalCustomers - customerRepository.countByCreatedAtBetween(monthStart, now);

                long activeCustomers = customerRepository.countActiveCustomersWithSales(monthStart, now);
                long prevActive = customerRepository.countActiveCustomersWithSales(prevMonthStart, prevMonthEnd);

                // ── Ventas del mes actual ────────────────────────────────────────
                List<Object[]> salesSummary = customerRepository.findCustomerSalesSummary(monthStart, now);
                BigDecimal totalSalesAmount = salesSummary.stream()
                                .map(r -> (BigDecimal) r[1])
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<Object[]> prevSalesSummary = customerRepository.findCustomerSalesSummary(prevMonthStart,
                                prevMonthEnd);
                BigDecimal prevSalesAmount = prevSalesSummary.stream()
                                .map(r -> (BigDecimal) r[1])
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Ticket promedio
                BigDecimal avgTicket = totalSalesAmount.compareTo(BigDecimal.ZERO) > 0 && !salesSummary.isEmpty()
                                ? totalSalesAmount.divide(BigDecimal.valueOf(salesSummary.size()), 2,
                                                RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                BigDecimal prevAvgTicket = prevSalesAmount.compareTo(BigDecimal.ZERO) > 0 && !prevSalesSummary.isEmpty()
                                ? prevSalesAmount.divide(BigDecimal.valueOf(prevSalesSummary.size()), 2,
                                                RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Frecuencia promedio (ventas/cliente)
                double avgFreq = salesSummary.isEmpty() ? 0
                                : salesSummary.stream().mapToLong(r -> ((Number) r[2]).longValue()).average().orElse(0);
                double prevAvgFreq = prevSalesSummary.isEmpty() ? 0
                                : prevSalesSummary.stream().mapToLong(r -> ((Number) r[2]).longValue()).average()
                                                .orElse(0);

                // ── Tendencias ───────────────────────────────────────────────────
                double totalTrend = calcTrend(prevTotal, totalCustomers);
                double activeTrend = calcTrend(prevActive, activeCustomers);
                double salesTrend = calcTrend(prevSalesAmount, totalSalesAmount);
                double ticketTrend = calcTrend(prevAvgTicket, avgTicket);
                double freqTrend = prevAvgFreq == 0 ? 0 : ((avgFreq - prevAvgFreq) / prevAvgFreq) * 100;

                // ── Segmentación ─────────────────────────────────────────────────
                // Basada en frecuencia de compras en el mes
                Map<Long, Long> salesCountPerCustomer = salesSummary.stream()
                                .collect(Collectors.toMap(
                                                r -> ((Number) r[0]).longValue(),
                                                r -> ((Number) r[2]).longValue()));

                long frequentCount = salesCountPerCustomer.values().stream().filter(c -> c >= 4).count();
                long occasionalCount = salesCountPerCustomer.values().stream().filter(c -> c >= 2 && c < 4).count();
                long newCount = customerRepository.countByCreatedAtBetween(monthStart, now);

                // VIP: top 10% por monto
                List<Object[]> topRaw = customerRepository.findTopCustomersByAmount(
                                now.minusYears(1), now, PageRequest.of(0, 500));
                long vipCount = Math.max(1, Math.round(topRaw.size() * 0.07));

                // Inactivos: clientes totales - activos este mes
                long inactiveCount = Math.max(0, totalCustomers - activeCustomers - newCount);

                // ── Top clientes ──────────────────────────────────────────────────
                List<Object[]> top10Raw = customerRepository.findTopCustomersByAmount(
                                monthStart, now, PageRequest.of(0, 10));
                BigDecimal topTotal = top10Raw.stream().map(r -> (BigDecimal) r[2]).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                List<TopCustomerItem> topCustomers = new ArrayList<>();
                for (int i = 0; i < top10Raw.size(); i++) {
                        Object[] r = top10Raw.get(i);
                        BigDecimal amount = (BigDecimal) r[2];
                        double pct = topTotal.compareTo(BigDecimal.ZERO) > 0
                                        ? amount.divide(topTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100
                                        : 0;
                        topCustomers.add(new TopCustomerItem(
                                        ((Number) r[0]).longValue(),
                                        (String) r[1],
                                        amount,
                                        ((Number) r[3]).intValue(),
                                        Math.round(pct * 10.0) / 10.0));
                }

                // ── Clientes recientes ────────────────────────────────────────────
                List<Object[]> recentRaw = customerRepository.findRecentCustomerPurchases(PageRequest.of(0, 5));
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "PE"));
                List<RecentCustomerItem> recentCustomers = new ArrayList<>();
                for (int i = 0; i < recentRaw.size(); i++) {
                        Object[] r = recentRaw.get(i);
                        long custId = ((Number) r[0]).longValue();
                        String name = (String) r[1];
                        String initials = buildInitials(name);
                        String phone = r[3] != null ? (String) r[3] : "";
                        String email = r[4] != null ? (String) r[4] : "";
                        LocalDateTime lastDate = (LocalDateTime) r[5];
                        BigDecimal totalPurchases = (BigDecimal) r[6];
                        long salesCount = ((Number) r[7]).longValue();

                        String status = salesCount >= 4 ? "VIP" : (salesCount >= 2 ? "ACTIVO" : "NUEVO");
                        String color = AVATAR_COLORS[i % AVATAR_COLORS.length];

                        recentCustomers.add(new RecentCustomerItem(
                                        custId, name, initials, color,
                                        phone, email,
                                        lastDate.format(fmt),
                                        totalPurchases, status,
                                        (String) r[2]));
                }

                // ── Actividad mensual (últimos 4 meses) ───────────────────────────
                LocalDateTime actStart = now.minusMonths(4).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                                .withNano(0);
                List<Object[]> newByMonth = customerRepository.findNewCustomersByMonth(actStart, now);
                List<Object[]> activeByMonth = customerRepository.findActiveCustomersByMonth(actStart, now);

                Map<String, Long> newMap = newByMonth.stream()
                                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));
                Map<String, Long> activeMap = activeByMonth.stream()
                                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).longValue()));

                Set<String> allMonths = new TreeSet<>();
                allMonths.addAll(newMap.keySet());
                allMonths.addAll(activeMap.keySet());

                List<ActivityPoint> activitySeries = allMonths.stream()
                                .map(m -> new ActivityPoint(m,
                                                newMap.getOrDefault(m, 0L),
                                                activeMap.getOrDefault(m, 0L)))
                                .collect(Collectors.toList());

                // ── Rango por puntos acumulados como proxy ────────────────────────
                List<Customer> allCustomers = customerRepository.findAll();
                long r0_499 = allCustomers.stream().filter(c -> c.getAccumulatedPoints() < 500).count();
                long r500_999 = allCustomers.stream()
                                .filter(c -> c.getAccumulatedPoints() >= 500 && c.getAccumulatedPoints() < 1000)
                                .count();
                long r1000_2499 = allCustomers.stream()
                                .filter(c -> c.getAccumulatedPoints() >= 1000 && c.getAccumulatedPoints() < 2500)
                                .count();
                long r2500_4999 = allCustomers.stream()
                                .filter(c -> c.getAccumulatedPoints() >= 2500 && c.getAccumulatedPoints() < 5000)
                                .count();
                long r5000_9999 = allCustomers.stream()
                                .filter(c -> c.getAccumulatedPoints() >= 5000 && c.getAccumulatedPoints() < 10000)
                                .count();
                long r10000plus = allCustomers.stream().filter(c -> c.getAccumulatedPoints() >= 10000).count();

                List<AgeRangeItem> ageRanges = List.of(
                                new AgeRangeItem("0-499 pts", r0_499),
                                new AgeRangeItem("500-999 pts", r500_999),
                                new AgeRangeItem("1k-2.4k pts", r1000_2499),
                                new AgeRangeItem("2.5k-4.9k pts", r2500_4999),
                                new AgeRangeItem("5k-9.9k pts", r5000_9999),
                                new AgeRangeItem("10k+ pts", r10000plus));

                return new CustomerDashboardResponse(
                                totalCustomers, activeCustomers,
                                totalSalesAmount, avgTicket, Math.round(avgFreq * 10.0) / 10.0,
                                totalTrend, activeTrend, salesTrend, ticketTrend, freqTrend,
                                recentCustomers,
                                frequentCount, occasionalCount, newCount, inactiveCount, vipCount,
                                topCustomers, activitySeries, ageRanges);
        }

        // ── Helpers ─────────────────────────────────────────────────────────────

        private double calcTrend(long prev, long current) {
                if (prev == 0)
                        return current > 0 ? 100 : 0;
                return ((double) (current - prev) / prev) * 100;
        }

        private double calcTrend(BigDecimal prev, BigDecimal current) {
                if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0)
                        return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
                return current.subtract(prev).divide(prev, 4, RoundingMode.HALF_UP).doubleValue() * 100;
        }

        private String buildInitials(String name) {
                if (name == null || name.isBlank())
                        return "?";
                String[] parts = name.trim().split("\\s+");
                if (parts.length == 1)
                        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
}
