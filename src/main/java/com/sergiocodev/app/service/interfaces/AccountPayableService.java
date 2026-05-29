package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.accountpayable.AccountPayablePaymentRequest;
import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.model.AccountPayable;

import java.util.List;

import com.sergiocodev.app.dto.accountpayable.AccountPayableDashboardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountPayableService {
    List<AccountPayableResponse> getAll();
    
    Page<AccountPayableResponse> getAllPaged(String supplierName, String purchaseIdentifier, String createdAt, String dueDate, String status, Pageable pageable);

    List<AccountPayableResponse> getBySupplierId(Long supplierId);

    List<AccountPayableResponse> getByStatus(AccountPayable.PayableStatus status);

    AccountPayableResponse pay(Long accountPayableId, AccountPayablePaymentRequest request, Long userId);
    
    List<AccountPayableDashboardResponse> getDashboard();
}
