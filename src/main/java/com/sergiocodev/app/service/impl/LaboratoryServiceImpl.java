package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.LaboratoryService;

import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import com.sergiocodev.app.mapper.LaboratoryMapper;
import com.sergiocodev.app.model.Laboratory;
import com.sergiocodev.app.repository.LaboratoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LaboratoryRepository repository;
    private final LaboratoryMapper mapper;

    @Override
    @Transactional
    public LaboratoryResponse createNewLaboratory(LaboratoryRequest request) {
        Laboratory entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaboratoryResponse> getAllLaboratory() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LaboratoryResponse> findAllPaged(String name, Pageable pageable) {
        return repository.findAllPaged(name, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LaboratoryResponse getLaboratoryById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Laboratory not found"));
    }

    @Override
    @Transactional
    public LaboratoryResponse updateLaboratoryById(Long id, LaboratoryRequest request) {
        Laboratory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratory not found"));
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLaboratoryById(Long id) {
        repository.deleteById(id);
    }
}
