package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.accountreceivable.AccountReceivableRequest;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableResponse;
import com.sergiocodev.app.dto.accountreceivable.AccountReceivableDashboardResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface AccountReceivableService {
    AccountReceivableResponse create(AccountReceivableRequest request);
    List<AccountReceivableResponse> getAll();
    Page<AccountReceivableResponse> getAllPaged(String customerName, String saleIdentifier, String createdAt, String dueDate, String status, Pageable pageable);
    List<AccountReceivableResponse> getByCustomerId(Long customerId);

    AccountReceivableResponse getById(Long id);

    void cancel(Long id);

    List<AccountReceivableDashboardResponse> getDashboard();
}
