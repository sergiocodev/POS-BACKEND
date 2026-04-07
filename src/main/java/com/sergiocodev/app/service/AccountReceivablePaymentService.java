package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface AccountReceivablePaymentService {
    AccountReceivablePaymentResponse create(AccountReceivablePaymentRequest request, Long userId);

    List<AccountReceivablePaymentResponse> getByAccountReceivableId(Long accountReceivableId);

    Page<AccountReceivablePaymentResponse> getHistory(
            LocalDate startDate,
            LocalDate endDate,
            Long customerId,
            String paymentMethod,
            Pageable pageable);

    void cancel(Long id);
}
