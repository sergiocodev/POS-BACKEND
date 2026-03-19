package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.model.AccountPayable;

import java.math.BigDecimal;
import java.util.List;

public interface AccountPayableService {
    List<AccountPayableResponse> getAll();

    List<AccountPayableResponse> getBySupplierId(Long supplierId);

    List<AccountPayableResponse> getByStatus(AccountPayable.PayableStatus status);

    AccountPayableResponse pay(Long accountPayableId, BigDecimal amount, Long userId);
}
