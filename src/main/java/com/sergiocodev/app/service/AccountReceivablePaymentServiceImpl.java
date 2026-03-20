package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentResponse;
import com.sergiocodev.app.model.AccountReceivable;
import com.sergiocodev.app.model.AccountReceivablePayment;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.AccountReceivablePaymentRepository;
import com.sergiocodev.app.repository.AccountReceivableRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
import com.sergiocodev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountReceivablePaymentServiceImpl implements AccountReceivablePaymentService {

    private final AccountReceivablePaymentRepository paymentRepository;
    private final AccountReceivableRepository receivableRepository;
    private final CashSessionRepository cashSessionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AccountReceivablePaymentResponse create(AccountReceivablePaymentRequest request, Long userId) {
        AccountReceivable receivable = receivableRepository.findById(request.accountReceivableId())
                .orElseThrow(() -> new RuntimeException("Account Receivable not found"));
        CashSession cashSession = cashSessionRepository.findById(request.cashSessionId())
                .orElseThrow(() -> new RuntimeException("Cash Session not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (receivable.getStatus() == AccountReceivable.ReceivableStatus.PAID
                || receivable.getStatus() == AccountReceivable.ReceivableStatus.CANCELED) {
            throw new RuntimeException("Cannot make payment on a " + receivable.getStatus() + " account");
        }

        if (request.amount().compareTo(receivable.getPendingBalance()) > 0) {
            throw new RuntimeException("Payment amount exceeds pending balance");
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
    @Transactional
    public void cancel(Long id) {
        AccountReceivablePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getDeletedAt() != null) {
            throw new RuntimeException("Payment already canceled");
        }

        AccountReceivable receivable = payment.getAccountReceivable();

        // Revert balance
        receivable.setAmountPaid(receivable.getAmountPaid().subtract(payment.getAmount()));
        receivable.setPendingBalance(receivable.getPendingBalance().add(payment.getAmount()));

        if (receivable.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) {
            receivable.setStatus(AccountReceivable.ReceivableStatus.PENDING);
        } else {
            receivable.setStatus(AccountReceivable.ReceivableStatus.PARTIAL);
        }
        receivableRepository.save(receivable);

        payment.setDeletedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private AccountReceivablePaymentResponse mapToResponse(AccountReceivablePayment payment) {
        return new AccountReceivablePaymentResponse(
                payment.getId(),
                payment.getAccountReceivable().getId(),
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
