package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import java.util.List;

public interface SaleService {
    SaleResponse create(SaleRequest request, Long userId);

    List<SaleResponse> getAll();

    SaleResponse getById(Long id);

    void cancel(Long id);
}
