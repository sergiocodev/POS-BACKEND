package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.supplier.SupplierRequest;
import com.sergiocodev.app.dto.supplier.SupplierResponse;
import java.util.List;

public interface SupplierService {
    SupplierResponse create(SupplierRequest request);

    List<SupplierResponse> getAll();

    SupplierResponse getById(Long id);

    SupplierResponse update(Long id, SupplierRequest request);

    void delete(Long id);
}
