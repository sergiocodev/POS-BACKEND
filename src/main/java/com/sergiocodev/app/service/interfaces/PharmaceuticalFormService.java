package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormRequest;
import com.sergiocodev.app.dto.pharmaceuticalform.PharmaceuticalFormResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PharmaceuticalFormService {
    List<PharmaceuticalFormResponse> findAll();

    Page<PharmaceuticalFormResponse> findAllPaged(String name, Pageable pageable);

    PharmaceuticalFormResponse findById(Long id);

    PharmaceuticalFormResponse create(PharmaceuticalFormRequest request);

    PharmaceuticalFormResponse update(Long id, PharmaceuticalFormRequest request);

    void delete(Long id);
}
