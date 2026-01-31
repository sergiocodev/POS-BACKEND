package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.presentation.PresentationRequest;
import com.sergiocodev.app.dto.presentation.PresentationResponse;
import com.sergiocodev.app.model.Presentation;
import com.sergiocodev.app.repository.PresentationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresentationServiceImpl implements PresentationService {

    private final PresentationRepository repository;

    @Override
    @Transactional
    public PresentationResponse create(PresentationRequest request) {
        Presentation entity = new Presentation();
        entity.setDescription(request.getDescription());
        return new PresentationResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresentationResponse> getAll() {
        return repository.findAll().stream()
                .map(PresentationResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PresentationResponse getById(Long id) {
        return repository.findById(id)
                .map(PresentationResponse::new)
                .orElseThrow(() -> new RuntimeException("Presentation not found"));
    }

    @Override
    @Transactional
    public PresentationResponse update(Long id, PresentationRequest request) {
        Presentation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentation not found"));
        entity.setDescription(request.getDescription());
        return new PresentationResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
