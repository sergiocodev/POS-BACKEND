package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivableRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableResponse;

import java.util.List;

public interface AccountReceivableService {
    AccountReceivableResponse create(AccountReceivableRequest request);
    List<AccountReceivableResponse> getAll();
    List<AccountReceivableResponse> getByCustomerId(Long customerId);

    AccountReceivableResponse getById(Long id);

    void cancel(Long id);
}
