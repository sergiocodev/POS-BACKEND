package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.mapper.AccountPayableMapper;
import com.sergiocodev.app.model.AccountPayable;
import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.repository.AccountPayableRepository;
import com.sergiocodev.app.repository.CashMovementRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountPayableServiceImpl implements AccountPayableService {

    private final AccountPayableRepository repository;
    private final AccountPayableMapper mapper;
    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;

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
    public AccountPayableResponse pay(Long accountPayableId, BigDecimal amount, Long userId) {
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

        // Registrar salida de caja
        CashSession session = cashSessionRepository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró una sesión de caja abierta para el usuario"));

        session.setCalculatedBalance(session.getCalculatedBalance().subtract(amount));
        cashSessionRepository.save(session);

        CashMovement movement = new CashMovement();
        movement.setCashSession(session);
        movement.setAmount(amount);
        movement.setType(CashMovement.MovementType.EXPENSE);
        movement.setReferenceTable("account_payables");
        movement.setReferenceId(account.getId());
        movement.setDescription(
                "Pago de cuenta por cobrar parcial o total - Proveedor: " + account.getSupplier().getName());
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
}
