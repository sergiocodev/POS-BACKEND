package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface AccountPayablePaymentService {
    AccountPayablePaymentResponse create(AccountPayablePaymentRequest request, Long userId);

    List<AccountPayablePaymentResponse> getByAccountPayableId(Long accountPayableId);

    Page<AccountPayablePaymentResponse> getHistory(
            LocalDate startDate,
            LocalDate endDate,
            Long supplierId,
            String paymentMethod,
            Pageable pageable);

    void cancel(Long id);
}
