package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.supplier.SupplierRequest;
import com.sergiocodev.app.dto.supplier.SupplierResponse;
import java.util.List;

public interface SupplierService {
    SupplierResponse create(SupplierRequest request);

    List<SupplierResponse> getAll();

    SupplierResponse getById(Long id);

    SupplierResponse update(Long id, SupplierRequest request);

    void delete(Long id);

    org.springframework.data.domain.Page<com.sergiocodev.app.dto.supplier.SupplierDetailResponse> getSupplierDetailsPaged(String providerInfo, String category, String contactInfo, org.springframework.data.domain.Pageable pageable);

    java.util.List<com.sergiocodev.app.dto.supplier.SupplierSummaryResponse> getSummary(Long establishmentId);
}
