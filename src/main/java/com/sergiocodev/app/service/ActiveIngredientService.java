package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.activeingredient.ActiveIngredientRequest;
import com.sergiocodev.app.dto.activeingredient.ActiveIngredientResponse;
import java.util.List;

public interface ActiveIngredientService {
    ActiveIngredientResponse create(ActiveIngredientRequest request);

    List<ActiveIngredientResponse> getAll();

    ActiveIngredientResponse getById(Long id);

    ActiveIngredientResponse update(Long id, ActiveIngredientRequest request);

    void delete(Long id);

    List<ActiveIngredientResponse> search(String query);
}
