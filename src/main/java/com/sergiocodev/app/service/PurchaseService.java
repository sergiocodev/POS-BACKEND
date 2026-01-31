package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.purchase.PurchaseRequest;
import com.sergiocodev.app.dto.purchase.PurchaseResponse;
import java.util.List;

public interface PurchaseService {
    PurchaseResponse create(PurchaseRequest request, Long userId);

    List<PurchaseResponse> getAll();

    PurchaseResponse getById(Long id);

    void cancel(Long id);
}
