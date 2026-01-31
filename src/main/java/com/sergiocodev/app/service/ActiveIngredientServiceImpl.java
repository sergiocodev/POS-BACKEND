package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.activeingredient.ActiveIngredientRequest;
import com.sergiocodev.app.dto.activeingredient.ActiveIngredientResponse;
import com.sergiocodev.app.model.ActiveIngredient;
import com.sergiocodev.app.repository.ActiveIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActiveIngredientServiceImpl implements ActiveIngredientService {

    private final ActiveIngredientRepository repository;

    @Override
    @Transactional
    public ActiveIngredientResponse create(ActiveIngredientRequest request) {
        ActiveIngredient entity = new ActiveIngredient();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return new ActiveIngredientResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveIngredientResponse> getAll() {
        return repository.findAll().stream()
                .map(ActiveIngredientResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActiveIngredientResponse getById(Long id) {
        return repository.findById(id)
                .map(ActiveIngredientResponse::new)
                .orElseThrow(() -> new RuntimeException("Active ingredient not found"));
    }

    @Override
    @Transactional
    public ActiveIngredientResponse update(Long id, ActiveIngredientRequest request) {
        ActiveIngredient entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Active ingredient not found"));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return new ActiveIngredientResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
