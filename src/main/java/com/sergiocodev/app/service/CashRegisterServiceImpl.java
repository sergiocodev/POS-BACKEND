package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.cashregister.CashRegisterRequest;
import com.sergiocodev.app.dto.cashregister.CashRegisterResponse;
import com.sergiocodev.app.model.CashRegister;
import com.sergiocodev.app.repository.CashRegisterRepository;
import com.sergiocodev.app.repository.EstablishmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashRegisterServiceImpl implements CashRegisterService {

    private final CashRegisterRepository repository;
    private final EstablishmentRepository establishmentRepository;

    @Override
    @Transactional
    public CashRegisterResponse create(CashRegisterRequest request) {
        CashRegister entity = new CashRegister();
        entity.setName(request.getName());
        entity.setEstablishment(establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new RuntimeException("Establishment not found")));
        entity.setActive(request.isActive());
        return new CashRegisterResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashRegisterResponse> getAll() {
        return repository.findAll().stream()
                .map(CashRegisterResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CashRegisterResponse getById(Long id) {
        return repository.findById(id)
                .map(CashRegisterResponse::new)
                .orElseThrow(() -> new RuntimeException("Cash register not found"));
    }

    @Override
    @Transactional
    public CashRegisterResponse update(Long id, CashRegisterRequest request) {
        CashRegister entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cash register not found"));
        entity.setName(request.getName());
        entity.setEstablishment(establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new RuntimeException("Establishment not found")));
        entity.setActive(request.isActive());
        return new CashRegisterResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
