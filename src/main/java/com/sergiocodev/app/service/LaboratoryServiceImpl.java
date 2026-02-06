package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import com.sergiocodev.app.mapper.LaboratoryMapper;
import com.sergiocodev.app.model.Laboratory;
import com.sergiocodev.app.repository.LaboratoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LaboratoryRepository repository;
    private final LaboratoryMapper mapper;

    @Override
    @Transactional
    public LaboratoryResponse create(LaboratoryRequest request) {
        Laboratory entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaboratoryResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LaboratoryResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Laboratory not found"));
    }

    @Override
    @Transactional
    public LaboratoryResponse update(Long id, LaboratoryRequest request) {
        Laboratory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratory not found"));
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
