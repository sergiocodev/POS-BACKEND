package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentResponse;
import com.sergiocodev.app.model.AccountPayable;
import com.sergiocodev.app.model.AccountPayablePayment;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.AccountPayablePaymentRepository;
import com.sergiocodev.app.repository.AccountPayableRepository;
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
public class AccountPayablePaymentServiceImpl implements AccountPayablePaymentService {

    private final AccountPayablePaymentRepository paymentRepository;
    private final AccountPayableRepository payableRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AccountPayablePaymentResponse create(AccountPayablePaymentRequest request, Long userId) {
        AccountPayable payable = payableRepository.findById(request.accountPayableId())
                .orElseThrow(() -> new RuntimeException("AccountPayable not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (payable.getStatus() == AccountPayable.PayableStatus.PAID
                || payable.getStatus() == AccountPayable.PayableStatus.CANCELED) {
            throw new RuntimeException("Cannot make payment on a " + payable.getStatus() + " account");
        }

        if (request.amount().compareTo(payable.getPendingBalance()) > 0) {
            throw new RuntimeException("Payment amount exceeds pending balance");
        }

        AccountPayablePayment payment = new AccountPayablePayment();
        payment.setAccountPayable(payable);
        payment.setUser(user);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setReference(request.reference());
        payment.setNotes(request.notes());
        payment.setPaymentDate(LocalDateTime.now());

        AccountPayablePayment savedPayment = paymentRepository.save(payment);

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
    @Transactional
    public void cancel(Long id) {
        AccountPayablePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getDeletedAt() != null) {
            throw new RuntimeException("Payment already canceled");
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

        payment.setDeletedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private AccountPayablePaymentResponse mapToResponse(AccountPayablePayment payment) {
        return new AccountPayablePaymentResponse(
                payment.getId(),
                payment.getAccountPayable().getId(),
                payment.getUser().getId(),
                payment.getUser().getUsername(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getReference(),
                payment.getNotes(),
                payment.getPaymentDate());
    }
}
