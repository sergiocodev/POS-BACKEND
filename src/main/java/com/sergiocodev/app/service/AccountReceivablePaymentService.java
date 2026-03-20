package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivablePaymentResponse;

import java.util.List;

public interface AccountReceivablePaymentService {
    AccountReceivablePaymentResponse create(AccountReceivablePaymentRequest request, Long userId);

    List<AccountReceivablePaymentResponse> getByAccountReceivableId(Long accountReceivableId);

    void cancel(Long id);
}
