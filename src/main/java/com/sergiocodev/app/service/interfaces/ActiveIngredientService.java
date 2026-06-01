package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.activeingredient.ActiveIngredientRequest;
import com.sergiocodev.app.dto.activeingredient.ActiveIngredientResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActiveIngredientService {
    ActiveIngredientResponse create(ActiveIngredientRequest request);

    List<ActiveIngredientResponse> getAll();

    Page<ActiveIngredientResponse> findAllPaged(String name, String description, Pageable pageable);

    ActiveIngredientResponse getById(Long id);

    ActiveIngredientResponse update(Long id, ActiveIngredientRequest request);

    void delete(Long id);

    List<ActiveIngredientResponse> search(String query);
}
