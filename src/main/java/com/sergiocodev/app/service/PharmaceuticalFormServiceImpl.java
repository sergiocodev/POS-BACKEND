package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormRequest;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormResponse;
import com.sergiocodev.app.exception.PharmaceuticalFormNotFoundException;
import com.sergiocodev.app.mapper.PharmaceuticalFormMapper;
import com.sergiocodev.app.model.PharmaceuticalForm;
import com.sergiocodev.app.repository.PharmaceuticalFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmaceuticalFormServiceImpl implements PharmaceuticalFormService {

    private final PharmaceuticalFormRepository pharmaceuticalFormRepository;
    private final PharmaceuticalFormMapper pharmaceuticalFormMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PharmaceuticalFormResponse> findAll() {
        return pharmaceuticalFormRepository.findAll().stream()
                .map(pharmaceuticalFormMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PharmaceuticalFormResponse findById(Long id) {
        PharmaceuticalForm pharmaceuticalForm = pharmaceuticalFormRepository.findById(id)
                .orElseThrow(
                        () -> new PharmaceuticalFormNotFoundException("Pharmaceutical form not found with ID: " + id));
        return pharmaceuticalFormMapper.toResponse(pharmaceuticalForm);
    }

    @Override
    @Transactional
    public PharmaceuticalFormResponse create(PharmaceuticalFormRequest request) {
        // Here you might want to check for duplicates by name if needed, assuming
        // repository has existsByName
        // if (pharmaceuticalFormRepository.existsByName(request.name())) { ... }

        PharmaceuticalForm pharmaceuticalForm = pharmaceuticalFormMapper.toEntity(request);
        PharmaceuticalForm saved = pharmaceuticalFormRepository.save(pharmaceuticalForm);
        return pharmaceuticalFormMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PharmaceuticalFormResponse update(Long id, PharmaceuticalFormRequest request) {
        PharmaceuticalForm pharmaceuticalForm = pharmaceuticalFormRepository.findById(id)
                .orElseThrow(
                        () -> new PharmaceuticalFormNotFoundException("Pharmaceutical form not found with ID: " + id));

        pharmaceuticalFormMapper.updateEntity(request, pharmaceuticalForm);
        PharmaceuticalForm updated = pharmaceuticalFormRepository.save(pharmaceuticalForm);
        return pharmaceuticalFormMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!pharmaceuticalFormRepository.existsById(id)) {
            throw new PharmaceuticalFormNotFoundException("Pharmaceutical form not found with ID: " + id);
        }
        pharmaceuticalFormRepository.deleteById(id);
    }
}
