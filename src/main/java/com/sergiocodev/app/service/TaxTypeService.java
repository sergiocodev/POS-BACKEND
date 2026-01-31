package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.taxtype.TaxTypeRequest;
import com.sergiocodev.app.dto.taxtype.TaxTypeResponse;
import java.util.List;

public interface TaxTypeService {
    TaxTypeResponse create(TaxTypeRequest request);

    List<TaxTypeResponse> getAll();

    TaxTypeResponse getById(Long id);

    TaxTypeResponse update(Long id, TaxTypeRequest request);

    void delete(Long id);
}
