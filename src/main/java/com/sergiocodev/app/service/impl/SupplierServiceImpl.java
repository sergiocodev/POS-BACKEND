package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.SupplierService;

import com.sergiocodev.app.dto.supplier.SupplierRequest;
import com.sergiocodev.app.dto.supplier.SupplierResponse;
import com.sergiocodev.app.model.Supplier;
import com.sergiocodev.app.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier entity = new Supplier();
        entity.setName(request.name());
        entity.setRuc(request.ruc());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setAddress(request.address());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.contactName() != null) entity.setContactName(request.contactName());
        if (request.status() != null) entity.setStatus(request.status());
        if (request.rating() != null) entity.setRating(request.rating());
        return new SupplierResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {
        return repository.findAll().stream()
                .map(SupplierResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {
        return repository.findById(id)
                .map(SupplierResponse::new)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        entity.setName(request.name());
        entity.setRuc(request.ruc());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setAddress(request.address());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.contactName() != null) entity.setContactName(request.contactName());
        if (request.status() != null) entity.setStatus(request.status());
        if (request.rating() != null) entity.setRating(request.rating());
        return new SupplierResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.sergiocodev.app.dto.supplier.SupplierDetailResponse> getSupplierDetailsPaged(String providerInfo, String category, String contactInfo, org.springframework.data.domain.Pageable pageable) {
        return repository.getSupplierDetailsPaged(providerInfo, category, contactInfo, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public com.sergiocodev.app.dto.supplier.SupplierSummaryResponse getSummary() {
        return new com.sergiocodev.app.dto.supplier.SupplierSummaryResponse(
            repository.countActiveSuppliers(),
            repository.countEvaluatingSuppliers(),
            repository.countExpiredSuppliers(),
            repository.calculateTotalSpendCurrentYear(),
            repository.calculateAverageRating()
        );
    }
}
