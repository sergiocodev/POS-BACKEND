package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormRequest;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormResponse;
import java.util.List;

public interface PharmaceuticalFormService {
    List<PharmaceuticalFormResponse> findAll();

    PharmaceuticalFormResponse findById(Long id);

    PharmaceuticalFormResponse create(PharmaceuticalFormRequest request);

    PharmaceuticalFormResponse update(Long id, PharmaceuticalFormRequest request);

    void delete(Long id);
}
