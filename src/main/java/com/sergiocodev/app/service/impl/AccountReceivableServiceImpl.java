package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.AccountReceivableService;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivableRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableResponse;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableDashboardResponse;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.model.AccountReceivable;
import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.repository.AccountReceivableRepository;
import com.sergiocodev.app.repository.CustomerRepository;
import com.sergiocodev.app.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountReceivableServiceImpl implements AccountReceivableService {

    private final AccountReceivableRepository repository;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public AccountReceivableResponse create(AccountReceivableRequest request) {
        Sale sale = saleRepository.findById(request.saleId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (repository.findBySaleId(sale.getId()).isPresent()) {
            throw new BadRequestException("Account Receivable already exists for this sale");
        }

        AccountReceivable receivable = new AccountReceivable();
        receivable.setSale(sale);
        receivable.setCustomer(customer);
        receivable.setTotalAmount(request.totalAmount());
        receivable.setAmountPaid(BigDecimal.ZERO);
        receivable.setPendingBalance(request.totalAmount());
        receivable.setStatus(AccountReceivable.ReceivableStatus.PENDING);
        receivable.setDueDate(request.dueDate());
        receivable.setNotes(request.notes());

        return mapToResponse(repository.save(receivable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountReceivableResponse> getAllPaged(String customerName, String saleIdentifier, String createdAt, String dueDate, String status, Long establishmentId, Pageable pageable) {
        Specification<AccountReceivable> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (customerName != null && !customerName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("customer").get("name")), "%" + customerName.toLowerCase() + "%"));
            }

            if (saleIdentifier != null && !saleIdentifier.isBlank()) {
                String search = saleIdentifier.replace("-", "").toLowerCase();
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("sale").get("series")), "%" + search + "%"),
                    cb.like(cb.lower(root.get("sale").get("number")), "%" + search + "%"),
                    cb.like(cb.lower(cb.concat(cb.concat(root.get("sale").get("series"), "-"), root.get("sale").get("number"))), "%" + saleIdentifier.toLowerCase() + "%")
                ));
            }

            if (createdAt != null && !createdAt.isBlank()) {
                LocalDate date = parseDate(createdAt);
                if (date != null) {
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(23, 59, 59);
                    predicates.add(cb.between(root.get("createdAt"), startOfDay, endOfDay));
                }
            }

            if (dueDate != null && !dueDate.isBlank()) {
                LocalDate date = parseDate(dueDate);
                if (date != null) {
                    predicates.add(cb.and(
                        cb.greaterThanOrEqualTo(root.get("dueDate"), date),
                        cb.lessThanOrEqualTo(root.get("dueDate"), date)
                    ));
                }
            }

            if (status != null && !status.isBlank()) {
                String statusUpper = status.toUpperCase();
                AccountReceivable.ReceivableStatus mappedStatus = null;
                if (statusUpper.contains("PEND")) mappedStatus = AccountReceivable.ReceivableStatus.PENDING;
                else if (statusUpper.contains("PAG") || statusUpper.contains("PAID")) mappedStatus = AccountReceivable.ReceivableStatus.PAID;
                else if (statusUpper.contains("PARC") || statusUpper.contains("PART")) mappedStatus = AccountReceivable.ReceivableStatus.PARTIAL;
                else if (statusUpper.contains("ANUL") || statusUpper.contains("CANC")) mappedStatus = AccountReceivable.ReceivableStatus.CANCELED;
                else {
                    try { mappedStatus = AccountReceivable.ReceivableStatus.valueOf(statusUpper); }
                    catch (IllegalArgumentException e) {
                        log.debug("Estado no reconocido: '{}', ignorando filtro", statusUpper);
                    }
                }
                if (mappedStatus != null) {
                    predicates.add(cb.equal(root.get("status"), mappedStatus));
                }
            }

            if (establishmentId != null) {
                predicates.add(cb.equal(root.get("sale").get("establishment").get("id"), establishmentId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(this::mapToResponse);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        dateStr = dateStr.trim();

        // Handle ISO format: yyyy-MM-dd
        if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            try {
                String[] parts = dateStr.split("-"                );
                return LocalDate.of(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                );
            } catch (Exception e) {
                log.debug("Error al parsear fecha ISO '{}': {}", dateStr, e.getMessage());
            }
        }

        // Handle d/M/yyyy, dd/MM/yyyy, d/M (assumes current year)
        if (dateStr.matches("\\d{1,2}/\\d{1,2}(/\\d{4})?")) {
            try {
                String[] parts = dateStr.split("/");
                int day   = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year  = parts.length == 3 ? Integer.parseInt(parts[2]) : LocalDate.now().getYear();
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                log.debug("Error al parsear fecha d/M '{}': {}", dateStr, e.getMessage());
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountReceivableResponse> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountReceivableResponse> getByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountReceivableResponse getById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Account Receivable not found"));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        AccountReceivable receivable = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account Receivable not found"));

        receivable.setStatus(AccountReceivable.ReceivableStatus.CANCELED);
        repository.save(receivable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountReceivableDashboardResponse> getSummary(Long establishmentId) {
        List<AccountReceivable.ReceivableStatus> excludedStatuses = List.of(
            AccountReceivable.ReceivableStatus.PAID,
            AccountReceivable.ReceivableStatus.CANCELED
        );
        AccountReceivable.ReceivableStatus canceledStatus = AccountReceivable.ReceivableStatus.CANCELED;

        // Current overall totals
        BigDecimal totalPendiente = repository.getTotalPendingBalance(excludedStatuses, establishmentId);
        BigDecimal montoVencido = repository.getOverdueBalance(excludedStatuses, establishmentId);
        BigDecimal porVencer = repository.getAmountUpcomingDue(excludedStatuses, establishmentId);
        BigDecimal totalExpected = repository.getTotalExpectedAmount(canceledStatus, establishmentId);
        BigDecimal totalCollected = repository.getTotalCollectedAmount(canceledStatus, establishmentId);
        BigDecimal tasaEfectiva = totalExpected.compareTo(BigDecimal.ZERO) > 0 
            ? totalCollected.divide(totalExpected, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) 
            : BigDecimal.ZERO;

        // --- TREND CALCULATIONS (Current Month vs Last Month MTD) ---
        LocalDate today = LocalDate.now();
        LocalDateTime startThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endThisMonth = today.atTime(23, 59, 59);
        LocalDateTime startLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endLastMonth = today.minusMonths(1).atTime(23, 59, 59); // MTD of last month

        // Trend for Total Pending
        BigDecimal pendingThis = repository.getPendingBalanceCreatedBetween(startThisMonth, endThisMonth, excludedStatuses, establishmentId);
        BigDecimal pendingLast = repository.getPendingBalanceCreatedBetween(startLastMonth, endLastMonth, excludedStatuses, establishmentId);
        
        // Trend for Overdue
        BigDecimal overdueThis = repository.getOverdueBalanceDueBetween(startThisMonth.toLocalDate(), endThisMonth.toLocalDate(), excludedStatuses, establishmentId);
        BigDecimal overdueLast = repository.getOverdueBalanceDueBetween(startLastMonth.toLocalDate(), endLastMonth.toLocalDate(), excludedStatuses, establishmentId);

        // Trend for Count/Amount (Por Vencer)
        BigDecimal amountThis = repository.getAmountDueBetween(today, today.plusMonths(1), excludedStatuses, establishmentId);
        BigDecimal amountLast = repository.getAmountDueBetween(today.minusMonths(1), today, excludedStatuses, establishmentId);

        // Trend for Effective Rate
        BigDecimal expThis = repository.getExpectedAmountCreatedBetween(startThisMonth, endThisMonth, canceledStatus, establishmentId);
        BigDecimal collThis = repository.getCollectedAmountCreatedBetween(startThisMonth, endThisMonth, canceledStatus, establishmentId);
        BigDecimal rateThis = expThis.compareTo(BigDecimal.ZERO) > 0 ? collThis.divide(expThis, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO;

        BigDecimal expLast = repository.getExpectedAmountCreatedBetween(startLastMonth, endLastMonth, canceledStatus, establishmentId);
        BigDecimal collLast = repository.getCollectedAmountCreatedBetween(startLastMonth, endLastMonth, canceledStatus, establishmentId);
        BigDecimal rateLast = expLast.compareTo(BigDecimal.ZERO) > 0 ? collLast.divide(expLast, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO;

        return List.of(
            new AccountReceivableDashboardResponse(
                "TOTAL PENDIENTE",
                totalPendiente.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "S/ ", null,
                calculateTrend(pendingThis, pendingLast),
                calculateDirection(pendingThis, pendingLast, false),
                "vs. mes ant."
            ),
            new AccountReceivableDashboardResponse(
                "MONTO VENCIDO",
                montoVencido.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "S/ ", null,
                calculateTrend(overdueThis, overdueLast),
                calculateDirection(overdueThis, overdueLast, true), // Inverted: Increase in overdue is bad (down/red)
                "vs. mes ant."
            ),
            new AccountReceivableDashboardResponse(
                "POR VENCER",
                porVencer.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "S/ ", null,
                calculateTrend(amountThis, amountLast),
                calculateDirection(amountThis, amountLast, false),
                "vs. mes ant."
            ),
            new AccountReceivableDashboardResponse(
                "TASA EFECTIVA",
                tasaEfectiva.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                null, "%",
                calculateTrend(rateThis, rateLast),
                calculateDirection(rateThis, rateLast, false),
                "vs. mes ant."
            )
        );
    }

    private String calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0.0%";
        }
        BigDecimal diff = current.subtract(previous);
        BigDecimal percent = diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        String sign = percent.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + percent.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String calculateDirection(BigDecimal current, BigDecimal previous, boolean invert) {
        int cmp = current.compareTo(previous);
        if (cmp == 0) return "neutral";
        if (invert) {
            return cmp > 0 ? "down" : "up"; // If overdue increases, show red (down)
        }
        return cmp > 0 ? "up" : "down";
    }

    private AccountReceivableResponse mapToResponse(AccountReceivable entity) {
        AccountReceivable.ReceivableStatus status = entity.getStatus();
        Long daysUntilDue = null;
        if (status != AccountReceivable.ReceivableStatus.PAID &&
                status != AccountReceivable.ReceivableStatus.CANCELED) {
            daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), entity.getDueDate());
        }
        return new AccountReceivableResponse(
                entity.getId(),
                entity.getSale().getSeries() + "-" + entity.getSale().getNumber(),
                entity.getCustomer().getName(),
                entity.getTotalAmount(),
                entity.getAmountPaid(),
                entity.getPendingBalance(),
                status,
                daysUntilDue,
                entity.getDueDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
