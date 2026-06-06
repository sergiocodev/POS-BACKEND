package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.cashregister.CashRegisterRequest;
import com.sergiocodev.app.dto.cashregister.CashRegisterResponse;
import java.util.List;

public interface CashRegisterService {
    CashRegisterResponse create(CashRegisterRequest request);

    List<CashRegisterResponse> getAll(Long establishmentId);

    CashRegisterResponse getById(Long id);

    CashRegisterResponse update(Long id, CashRegisterRequest request);

    void delete(Long id);
}
