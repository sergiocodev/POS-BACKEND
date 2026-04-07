package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.mapper.AccountPayableMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountPayableServiceImpl implements AccountPayableService {

    private final AccountPayableRepository repository;
    private final AccountPayableMapper mapper;
    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final AccountPayablePaymentRepository accountPayablePaymentRepository;
    private final CashConceptRepository cashConceptRepository;
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

        // Registrar salida de caja solo si es efectivo
        if (request.paymentMethod() == AccountPayablePayment.PaymentMethod.EFECTIVO) {
            session.setCalculatedBalance(session.getCalculatedBalance().subtract(amount));
            cashSessionRepository.save(session);

            CashMovement movement = new CashMovement();
            movement.setCashSession(session);
            movement.setUser(session.getUser());
            movement.setAmount(amount);

            String methodStr = request.paymentMethod().name().toUpperCase();
            CashConcept concept = cashConceptRepository.findByType(CashConcept.ConceptType.OUT).stream()
                    .filter(c -> (c.getName().toLowerCase().contains("proveedor") || c.getName().toLowerCase().contains("pago"))
                            && c.getName().toUpperCase().contains(methodStr))
                    .findFirst()
                    .orElseGet(() -> {
                        CashConcept newConcept = new CashConcept();
                        newConcept.setName("PAGO PROVEEDOR " + methodStr);
                        newConcept.setType(CashConcept.ConceptType.OUT);
                        return cashConceptRepository.save(newConcept);
                    });

            movement.setCashConcept(concept);
            movement.setReference(request.reference());
            movement.setDescription(request.notes() != null && !request.notes().isEmpty() ? request.notes()
                    : "Pago de cuenta por pagar parcial o total - Proveedor: " + account.getSupplier().getName());
            cashMovementRepository.save(movement);
        }

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
}
