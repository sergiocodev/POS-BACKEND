package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentResponse;

import java.util.List;

public interface AccountPayablePaymentService {
    AccountPayablePaymentResponse create(AccountPayablePaymentRequest request, Long userId);

    List<AccountPayablePaymentResponse> getByAccountPayableId(Long accountPayableId);

    void cancel(Long id);
}
