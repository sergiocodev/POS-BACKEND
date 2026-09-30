package com.sergiocodev.app.service.impl;

import org.springframework.data.domain.PageImpl;
import com.sergiocodev.app.dto.maintenance.UnitOfMeasureRequest;
import com.sergiocodev.app.dto.maintenance.UnitOfMeasureResponse;
import com.sergiocodev.app.model.UnitOfMeasure;
import com.sergiocodev.app.repository.UnitOfMeasureRepository;
import com.sergiocodev.app.service.interfaces.UnitOfMeasureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional
    public UnitOfMeasureResponse create(UnitOfMeasureRequest request) {
        if (unitOfMeasureRepository.existsByNameIgnoreCase(request.name())) {
            throw new RuntimeException("Unit of Measure with name " + request.name() + " already exists");
        }
        
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setName(request.name());
        
        return mapToResponse(unitOfMeasureRepository.save(unit));
    }

    @Override
    @Transactional
    public UnitOfMeasureResponse update(Long id, UnitOfMeasureRequest request) {
        UnitOfMeasure unit = unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit of Measure not found"));

        if (!unit.getName().equalsIgnoreCase(request.name()) && 
            unitOfMeasureRepository.existsByNameIgnoreCase(request.name())) {
            throw new RuntimeException("Unit of Measure with name " + request.name() + " already exists");
        }

        unit.setName(request.name());
        return mapToResponse(unitOfMeasureRepository.save(unit));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!unitOfMeasureRepository.existsById(id)) {
            throw new RuntimeException("Unit of Measure not found");
        }
        unitOfMeasureRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureResponse getById(Long id) {
        return unitOfMeasureRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Unit of Measure not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponse> getAll() {
        return unitOfMeasureRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> getAllPaginated(Pageable pageable, String search) {
        Page<UnitOfMeasure> page;
        
        if (search != null && !search.trim().isEmpty()) {
            // Simplified search, normally you'd use a specification or custom query
            page = unitOfMeasureRepository.findAll(pageable); // Fallback for simple implementation
        } else {
            page = unitOfMeasureRepository.findAll(pageable);
        }

        List<UnitOfMeasureResponse> data = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(data, pageable, page.getTotalElements());
    }

    private UnitOfMeasureResponse mapToResponse(UnitOfMeasure unit) {
        return new UnitOfMeasureResponse(
                unit.getId(),
                unit.getName()
        );
    }
}
