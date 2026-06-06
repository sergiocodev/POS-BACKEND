package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.AccountReceivablePaymentService;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentResponse;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;

import com.sergiocodev.app.service.interfaces.CashConceptService;
import com.sergiocodev.app.service.interfaces.CashMovementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountReceivablePaymentServiceImpl implements AccountReceivablePaymentService {

    private final AccountReceivablePaymentRepository paymentRepository;
    private final AccountReceivableRepository receivableRepository;
    private final CashSessionRepository cashSessionRepository;
    private final UserRepository userRepository;
    private final CashMovementRepository cashMovementRepository;
    private final CashMovementService cashMovementService;
    private final CashConceptService cashConceptService;

    @Override
    @Transactional
    public AccountReceivablePaymentResponse create(AccountReceivablePaymentRequest request, Long userId) {
        AccountReceivable receivable = receivableRepository.findById(request.accountReceivableId())
                .orElseThrow(() -> new ResourceNotFoundException("Account Receivable not found"));
        CashSession cashSession = cashSessionRepository.findById(request.cashSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Cash Session not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (receivable.getStatus() == AccountReceivable.ReceivableStatus.PAID
                || receivable.getStatus() == AccountReceivable.ReceivableStatus.CANCELED) {
            throw new BadRequestException("Cannot make payment on a " + receivable.getStatus() + " account");
        }

        if (request.amount().compareTo(receivable.getPendingBalance()) > 0) {
            throw new BadRequestException("Payment amount exceeds pending balance");
        }

        if (!cashSession.getCashRegister().getEstablishment().getId().equals(receivable.getSale().getEstablishment().getId())) {
            throw new BadRequestException("La caja abierta pertenece a otra sucursal. Cierre la caja actual antes de operar en esta sucursal.");
        }

        AccountReceivablePayment payment = new AccountReceivablePayment();
        payment.setAccountReceivable(receivable);
        payment.setCashSession(cashSession);
        payment.setUser(user);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setReference(request.reference());
        payment.setNotes(request.notes());
        payment.setPaymentDate(LocalDateTime.now());

        AccountReceivablePayment savedPayment = paymentRepository.save(payment);

        // Register cash movement for all payment methods
        // Find or create income concept matching the payment method
        CashConcept concept = cashConceptService.findOrCreateReceivableConcept(request.paymentMethod().name());

        String description = request.notes() != null && !request.notes().isEmpty() ? request.notes()
                : "Cobro de cuenta por cobrar parcial o total (" + request.paymentMethod().name() + ") - Cliente: "
                        + receivable.getCustomer().getName();

        cashMovementService.registerInternalMovement(cashSession, user, concept, request.amount(), request.reference(),
                description);

        // Update receivable balances
        BigDecimal newAmountPaid = receivable.getAmountPaid().add(request.amount());
        BigDecimal newPendingBalance = receivable.getTotalAmount().subtract(newAmountPaid);

        receivable.setAmountPaid(newAmountPaid);
        receivable.setPendingBalance(newPendingBalance);

        if (newPendingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            receivable.setStatus(AccountReceivable.ReceivableStatus.PAID);
        } else {
            receivable.setStatus(AccountReceivable.ReceivableStatus.PARTIAL);
        }
        receivableRepository.save(receivable);

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountReceivablePaymentResponse> getByAccountReceivableId(Long accountReceivableId) {
        return paymentRepository.findByAccountReceivableIdAndDeletedAtIsNull(accountReceivableId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountReceivablePaymentResponse> getHistory(
            LocalDate startDate,
            LocalDate endDate,
            Long customerId,
            String paymentMethod,
            Pageable pageable) {

        Specification<AccountReceivablePayment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), endDate.atTime(LocalTime.MAX)));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("accountReceivable").get("customer").get("id"), customerId));
            }
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                predicates.add(cb.equal(root.get("paymentMethod"),
                        AccountReceivablePayment.PaymentMethod.valueOf(paymentMethod)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return paymentRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        AccountReceivablePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getDeletedAt() != null) {
            throw new BadRequestException("Payment already canceled");
        }

        AccountReceivable receivable = payment.getAccountReceivable();

        // Revert balance in receivable
        receivable.setAmountPaid(receivable.getAmountPaid().subtract(payment.getAmount()));
        receivable.setPendingBalance(receivable.getPendingBalance().add(payment.getAmount()));

        if (receivable.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) {
            receivable.setStatus(AccountReceivable.ReceivableStatus.PENDING);
        } else {
            receivable.setStatus(AccountReceivable.ReceivableStatus.PARTIAL);
        }
        receivableRepository.save(receivable);

        // Revert session balance and movement
        CashSession session = payment.getCashSession();
        // Find movement associated with this payment to delete it
        cashMovementRepository.findByCashSessionIdAndAmountAndReference(
                session.getId(), payment.getAmount(), payment.getReference())
                .ifPresent(m -> cashMovementService.delete(m.getId()));

        payment.setDeletedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private AccountReceivablePaymentResponse mapToResponse(AccountReceivablePayment payment) {
        AccountReceivable receivable = payment.getAccountReceivable();
        return new AccountReceivablePaymentResponse(
                payment.getId(),
                receivable.getId(),
                receivable.getCustomer().getName(),
                receivable.getTotalAmount(),
                receivable.getAmountPaid(),
                receivable.getPendingBalance(),
                payment.getCashSession().getId(),
                payment.getUser().getId(),
                payment.getUser().getUsername(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getReference(),
                payment.getNotes(),
                payment.getPaymentDate());
    }
}
