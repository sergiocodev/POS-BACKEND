package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.establishment.EstablishmentRequest;
import com.sergiocodev.app.dto.establishment.EstablishmentResponse;
import com.sergiocodev.app.model.Establishment;
import com.sergiocodev.app.repository.EstablishmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstablishmentServiceImpl implements EstablishmentService {

    private final EstablishmentRepository repository;

    @Override
    @Transactional
    public EstablishmentResponse create(EstablishmentRequest request) {
        Establishment entity = new Establishment();
        entity.setName(request.name());
        entity.setAddress(request.address());
        entity.setCodeSunat(request.codeSunat());
        entity.setActive(request.active());
        return new EstablishmentResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablishmentResponse> getAll() {
        return repository.findAll().stream()
                .map(EstablishmentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstablishmentResponse getById(Long id) {
        return repository.findById(id)
                .map(EstablishmentResponse::new)
                .orElseThrow(() -> new RuntimeException("Establishment not found"));
    }

    @Override
    @Transactional
    public EstablishmentResponse update(Long id, EstablishmentRequest request) {
        Establishment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Establishment not found"));
        entity.setName(request.name());
        entity.setAddress(request.address());
        entity.setCodeSunat(request.codeSunat());
        entity.setActive(request.active());
        return new EstablishmentResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
