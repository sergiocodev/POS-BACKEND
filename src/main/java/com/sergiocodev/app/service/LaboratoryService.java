package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import java.util.List;

public interface LaboratoryService {
    LaboratoryResponse create(LaboratoryRequest request);

    List<LaboratoryResponse> getAll();

    LaboratoryResponse getById(Long id);

    LaboratoryResponse update(Long id, LaboratoryRequest request);

    void delete(Long id);
}
