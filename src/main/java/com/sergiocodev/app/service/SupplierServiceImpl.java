package com.sergiocodev.app.service;

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
        entity.setActive(request.active());
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
        entity.setActive(request.active());
        return new SupplierResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
