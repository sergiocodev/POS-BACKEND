package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.AccountPayableService;

import com.sergiocodev.app.service.interfaces.CashConceptService;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.mapper.AccountPayableMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import com.sergiocodev.app.dto.accountpayable.AccountPayableDashboardResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountPayableServiceImpl implements AccountPayableService {

    private final AccountPayableRepository repository;
    private final AccountPayableMapper mapper;
    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final AccountPayablePaymentRepository accountPayablePaymentRepository;
    private final CashConceptService cashConceptService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountPayableResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountPayableResponse> getBySupplierId(Long supplierId) {
        return repository.findBySupplierId(supplierId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountPayableResponse> getByStatus(AccountPayable.PayableStatus status) {
        return repository.findByStatus(status).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountPayableResponse pay(Long accountPayableId, AccountPayablePaymentRequest request, Long userId) {
        BigDecimal amount = request.amount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero");
        }

        AccountPayable account = repository.findById(accountPayableId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por pagar no encontrada"));

        if (account.getStatus() == AccountPayable.PayableStatus.PAID) {
            throw new IllegalArgumentException("Esta cuenta por pagar ya está completamente pagada");
        }

        if (amount.compareTo(account.getPendingBalance()) > 0) {
            throw new IllegalArgumentException(
                    "El monto a pagar no puede ser mayor al saldo pendiente (" + account.getPendingBalance() + ")");
        }

        CashSession session = cashSessionRepository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró una sesión de caja abierta para el usuario"));

        // Registrar pago
        AccountPayablePayment payment = new AccountPayablePayment();
        payment.setAccountPayable(account);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        payment.setUser(user);
        payment.setCashSession(session);

        payment.setAmount(amount);
        payment.setPaymentMethod(request.paymentMethod());
        payment.setReference(request.reference());
        payment.setNotes(request.notes());
        payment.setPaymentDate(LocalDateTime.now());
        accountPayablePaymentRepository.save(payment);

        // Registrar salida de caja
        if (request.paymentMethod() == AccountPayablePayment.PaymentMethod.EFECTIVO) {
            session.setCalculatedBalance(session.getCalculatedBalance().subtract(amount));
            cashSessionRepository.save(session);
        }

        CashMovement movement = new CashMovement();
        movement.setCashSession(session);
        movement.setUser(session.getUser());
        movement.setAmount(amount);

        CashConcept concept = cashConceptService.findOrCreatePayableConcept(request.paymentMethod().name());

        movement.setCashConcept(concept);
        movement.setReference(request.reference());
        movement.setDescription(request.notes() != null && !request.notes().isEmpty() ? request.notes()
                : "Pago de cuenta por pagar parcial o total - Proveedor: " + account.getSupplier().getName());
        cashMovementRepository.save(movement);

        // Actualizar la cuenta por pagar
        account.setAmountPaid(account.getAmountPaid().add(amount));
        account.setPendingBalance(account.getPendingBalance().subtract(amount));

        if (account.getPendingBalance().compareTo(BigDecimal.ZERO) == 0) {
            account.setStatus(AccountPayable.PayableStatus.PAID);
        } else {
            account.setStatus(AccountPayable.PayableStatus.PARTIAL);
        }

        AccountPayable updated = repository.save(account);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountPayableResponse> getAllPaged(String supplierName, String purchaseIdentifier, String createdAt, String dueDate, String status, Long establishmentId, Pageable pageable) {
        Specification<AccountPayable> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (supplierName != null && !supplierName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("supplier").get("name")), "%" + supplierName.toLowerCase() + "%"));
            }

            if (purchaseIdentifier != null && !purchaseIdentifier.isBlank()) {
                String search = purchaseIdentifier.replace("-", "").toLowerCase();
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("purchase").get("series")), "%" + search + "%"),
                    cb.like(cb.lower(root.get("purchase").get("number")), "%" + search + "%"),
                    cb.like(cb.lower(cb.concat(cb.concat(root.get("purchase").get("series"), "-"), root.get("purchase").get("number"))), "%" + purchaseIdentifier.toLowerCase() + "%")
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
                AccountPayable.PayableStatus mappedStatus = null;
                if (statusUpper.contains("PEND")) mappedStatus = AccountPayable.PayableStatus.PENDING;
                else if (statusUpper.contains("PAG") || statusUpper.contains("PAID")) mappedStatus = AccountPayable.PayableStatus.PAID;
                else if (statusUpper.contains("PARC") || statusUpper.contains("PART")) mappedStatus = AccountPayable.PayableStatus.PARTIAL;
                else if (statusUpper.contains("ANUL") || statusUpper.contains("CANC")) mappedStatus = AccountPayable.PayableStatus.CANCELED;
                else {
                    try { mappedStatus = AccountPayable.PayableStatus.valueOf(statusUpper); }
                    catch (IllegalArgumentException e) {
                        log.debug("Estado no reconocido: '{}', ignorando filtro", statusUpper);
                    }
                }
                if (mappedStatus != null) {
                    predicates.add(cb.equal(root.get("status"), mappedStatus));
                }
            }

            if (establishmentId != null) {
                predicates.add(cb.equal(root.get("purchase").get("establishment").get("id"), establishmentId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        dateStr = dateStr.trim();

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
    public List<AccountPayableDashboardResponse> getDashboard(Long establishmentId) {
        List<AccountPayable.PayableStatus> excludedStatuses = List.of(
            AccountPayable.PayableStatus.PAID,
            AccountPayable.PayableStatus.CANCELED
        );
        AccountPayable.PayableStatus canceledStatus = AccountPayable.PayableStatus.CANCELED;

        BigDecimal totalPendiente = repository.getTotalPendingBalance(excludedStatuses, establishmentId);
        BigDecimal montoVencido = repository.getOverdueBalance(excludedStatuses, establishmentId);
        Long porVencer = repository.getCountUpcomingDue(excludedStatuses, establishmentId);
        BigDecimal totalExpected = repository.getTotalExpectedAmount(canceledStatus, establishmentId);
        BigDecimal totalCollected = repository.getTotalCollectedAmount(canceledStatus, establishmentId);
        BigDecimal tasaEfectiva = totalExpected.compareTo(BigDecimal.ZERO) > 0 
            ? totalCollected.divide(totalExpected, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) 
            : BigDecimal.ZERO;

        LocalDate today = LocalDate.now();
        LocalDateTime startThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endThisMonth = today.atTime(23, 59, 59);
        LocalDateTime startLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endLastMonth = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        BigDecimal pendingThis = repository.getPendingBalanceCreatedBetween(startThisMonth, endThisMonth, excludedStatuses, establishmentId);
        BigDecimal pendingLast = repository.getPendingBalanceCreatedBetween(startLastMonth, endLastMonth, excludedStatuses, establishmentId);
        
        BigDecimal overdueThis = repository.getOverdueBalanceDueBetween(startThisMonth.toLocalDate(), endThisMonth.toLocalDate(), excludedStatuses, establishmentId);
        BigDecimal overdueLast = repository.getOverdueBalanceDueBetween(startLastMonth.toLocalDate(), endLastMonth.toLocalDate(), excludedStatuses, establishmentId);

        Long countThis = repository.getCountDueBetween(today, today.plusMonths(1), excludedStatuses, establishmentId);
        Long countLast = repository.getCountDueBetween(today.minusMonths(1), today, excludedStatuses, establishmentId);

        BigDecimal expThis = repository.getExpectedAmountCreatedBetween(startThisMonth, endThisMonth, canceledStatus, establishmentId);
        BigDecimal collThis = repository.getCollectedAmountCreatedBetween(startThisMonth, endThisMonth, canceledStatus, establishmentId);
        BigDecimal rateThis = expThis.compareTo(BigDecimal.ZERO) > 0 ? collThis.divide(expThis, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO;

        BigDecimal expLast = repository.getExpectedAmountCreatedBetween(startLastMonth, endLastMonth, canceledStatus, establishmentId);
        BigDecimal collLast = repository.getCollectedAmountCreatedBetween(startLastMonth, endLastMonth, canceledStatus, establishmentId);
        BigDecimal rateLast = expLast.compareTo(BigDecimal.ZERO) > 0 ? collLast.divide(expLast, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO;

        return List.of(
            new AccountPayableDashboardResponse(
                "TOTAL PENDIENTE",
                totalPendiente.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "S/ ", null,
                calculateTrend(pendingThis, pendingLast),
                calculateDirection(pendingThis, pendingLast, false),
                "vs. mes ant."
            ),
            new AccountPayableDashboardResponse(
                "MONTO VENCIDO",
                montoVencido.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "S/ ", null,
                calculateTrend(overdueThis, overdueLast),
                calculateDirection(overdueThis, overdueLast, true),
                "vs. mes ant."
            ),
            new AccountPayableDashboardResponse(
                "POR VENCER",
                String.valueOf(porVencer),
                null, null,
                calculateTrend(new BigDecimal(countThis), new BigDecimal(countLast)),
                calculateDirection(new BigDecimal(countThis), new BigDecimal(countLast), false),
                "vs. mes ant."
            ),
            new AccountPayableDashboardResponse(
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
            return cmp > 0 ? "down" : "up";
        }
        return cmp > 0 ? "up" : "down";
    }
}
