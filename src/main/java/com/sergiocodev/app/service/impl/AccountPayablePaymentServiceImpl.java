package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.AccountPayablePaymentService;

import com.sergiocodev.app.service.interfaces.CashConceptService;
import com.sergiocodev.app.service.interfaces.CashMovementService;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class AccountPayablePaymentServiceImpl implements AccountPayablePaymentService {

    private final AccountPayablePaymentRepository paymentRepository;
    private final AccountPayableRepository payableRepository;
    private final UserRepository userRepository;
    private final CashSessionRepository cashSessionRepository;
    private final CashMovementService cashMovementService;
    private final CashConceptService cashConceptService;
    private final CashMovementRepository cashMovementRepository;

    @Override
    @Transactional
    public AccountPayablePaymentResponse create(AccountPayablePaymentRequest request, Long userId) {
        AccountPayable payable = payableRepository.findById(request.accountPayableId())
                .orElseThrow(() -> new ResourceNotFoundException("AccountPayable not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (payable.getStatus() == AccountPayable.PayableStatus.PAID
                || payable.getStatus() == AccountPayable.PayableStatus.CANCELED) {
            throw new BadRequestException("Cannot make payment on a " + payable.getStatus() + " account");
        }

        if (request.amount().compareTo(payable.getPendingBalance()) > 0) {
            throw new BadRequestException("Payment amount exceeds pending balance");
        }

        // Register cash movement for all payment methods
        CashSession session = cashSessionRepository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                .orElseThrow(() -> new BadRequestException("Debe tener una sesión de caja abierta para realizar pagos"));

        AccountPayablePayment payment = new AccountPayablePayment();
        payment.setAccountPayable(payable);
        payment.setUser(user);
        payment.setCashSession(session);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setReference(request.reference());
        payment.setNotes(request.notes());
        payment.setPaymentDate(LocalDateTime.now());

        AccountPayablePayment savedPayment = paymentRepository.save(payment);

        CashConcept concept = cashConceptService.findOrCreatePayableConcept(request.paymentMethod().name());

        String description = request.notes() != null && !request.notes().isEmpty() ? request.notes()
                : "Pago a proveedor (" + request.paymentMethod().name() + ") - Proveedor: "
                        + payable.getSupplier().getName();

        if (request.paymentMethod() == AccountPayablePayment.PaymentMethod.EFECTIVO) {
            session.setCalculatedBalance(session.getCalculatedBalance().subtract(request.amount()));
            cashSessionRepository.save(session);
        }

        CashMovement movement = new CashMovement();
        movement.setCashSession(session);
        movement.setUser(user);
        movement.setCashConcept(concept);
        movement.setAmount(request.amount());
        movement.setReference(request.reference());
        movement.setDescription(description);
        movement.setCreatedAt(LocalDateTime.now());
        cashMovementRepository.save(movement);

        // Update payable balances
        BigDecimal newAmountPaid = payable.getAmountPaid().add(request.amount());
        BigDecimal newPendingBalance = payable.getTotalAmount().subtract(newAmountPaid);

        payable.setAmountPaid(newAmountPaid);
        payable.setPendingBalance(newPendingBalance);

        if (newPendingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            payable.setStatus(AccountPayable.PayableStatus.PAID);
        } else {
            payable.setStatus(AccountPayable.PayableStatus.PARTIAL);
        }
        payableRepository.save(payable);

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountPayablePaymentResponse> getByAccountPayableId(Long accountPayableId) {
        return paymentRepository.findByAccountPayableIdAndDeletedAtIsNull(accountPayableId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountPayablePaymentResponse> getHistory(
            LocalDate startDate,
            LocalDate endDate,
            Long supplierId,
            String paymentMethod,
            Pageable pageable) {

        Specification<AccountPayablePayment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), endDate.atTime(LocalTime.MAX)));
            }
            if (supplierId != null) {
                predicates.add(cb.equal(root.get("accountPayable").get("supplier").get("id"), supplierId));
            }
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                predicates.add(cb.equal(root.get("paymentMethod"),
                        AccountPayablePayment.PaymentMethod.valueOf(paymentMethod)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return paymentRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        AccountPayablePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getDeletedAt() != null) {
            throw new BadRequestException("Payment already canceled");
        }

        AccountPayable payable = payment.getAccountPayable();

        // Revert balance
        payable.setAmountPaid(payable.getAmountPaid().subtract(payment.getAmount()));
        payable.setPendingBalance(payable.getPendingBalance().add(payment.getAmount()));

        if (payable.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) {
            payable.setStatus(AccountPayable.PayableStatus.PENDING);
        } else {
            payable.setStatus(AccountPayable.PayableStatus.PARTIAL);
        }
        payableRepository.save(payable);

        // Revert cash movement
        CashSession session = cashSessionRepository
                .findByUserIdAndStatus(payment.getUser().getId(), CashSession.SessionStatus.OPEN)
                .orElse(null); // If session is closed, we might still want to delete the movement record or
                               // handle it differently.

        if (session != null) {
            cashMovementRepository.findByCashSessionIdAndAmountAndReference(
                    session.getId(), payment.getAmount(), payment.getReference())
                    .ifPresent(m -> cashMovementService.delete(m.getId()));
        }

        payment.setDeletedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private AccountPayablePaymentResponse mapToResponse(AccountPayablePayment payment) {
        AccountPayable payable = payment.getAccountPayable();
        return new AccountPayablePaymentResponse(
                payment.getId(),
                payable.getId(),
                payable.getSupplier().getName(),
                payable.getTotalAmount(),
                payable.getAmountPaid(),
                payable.getPendingBalance(),
                payment.getUser().getId(),
                payment.getUser().getUsername(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getReference(),
                payment.getNotes(),
                payment.getPaymentDate());
    }
}
