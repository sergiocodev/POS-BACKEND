package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionRequest;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionResponse;
import com.sergiocodev.app.mapper.TherapeuticActionMapper;
import com.sergiocodev.app.model.TherapeuticAction;
import com.sergiocodev.app.repository.TherapeuticActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TherapeuticActionServiceImpl implements TherapeuticActionService {

    private final TherapeuticActionRepository repository;
    private final TherapeuticActionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TherapeuticActionResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TherapeuticActionResponse findById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Therapeutic Action not found"));
    }

    @Override
    @Transactional
    public TherapeuticActionResponse create(TherapeuticActionRequest request) {
        if (repository.existsByName(request.name())) {
            throw new RuntimeException("Therapeutic Action with name " + request.name() + " already exists");
        }
        TherapeuticAction entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public TherapeuticActionResponse update(Long id, TherapeuticActionRequest request) {
        TherapeuticAction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Therapeutic Action not found"));

        if (!entity.getName().equalsIgnoreCase(request.name()) && repository.existsByName(request.name())) {
            throw new RuntimeException("Therapeutic Action with name " + request.name() + " already exists");
        }

        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Therapeutic Action not found");
        }
        repository.deleteById(id);
    }
}
