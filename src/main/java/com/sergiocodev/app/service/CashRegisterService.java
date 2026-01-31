package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.cashregister.CashRegisterRequest;
import com.sergiocodev.app.dto.cashregister.CashRegisterResponse;
import java.util.List;

public interface CashRegisterService {
    CashRegisterResponse create(CashRegisterRequest request);

    List<CashRegisterResponse> getAll();

    CashRegisterResponse getById(Long id);

    CashRegisterResponse update(Long id, CashRegisterRequest request);

    void delete(Long id);
}
