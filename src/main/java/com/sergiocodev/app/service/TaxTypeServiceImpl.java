package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.taxtype.TaxTypeRequest;
import com.sergiocodev.app.dto.taxtype.TaxTypeResponse;
import com.sergiocodev.app.mapper.TaxTypeMapper;
import com.sergiocodev.app.model.TaxType;
import com.sergiocodev.app.repository.TaxTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxTypeServiceImpl implements TaxTypeService {

    private final TaxTypeRepository repository;
    private final TaxTypeMapper mapper;

    @Override
    @Transactional
    public TaxTypeResponse create(TaxTypeRequest request) {
        TaxType entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxTypeResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaxTypeResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Tax type not found"));
    }

    @Override
    @Transactional
    public TaxTypeResponse update(Long id, TaxTypeRequest request) {
        TaxType entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax type not found"));
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
