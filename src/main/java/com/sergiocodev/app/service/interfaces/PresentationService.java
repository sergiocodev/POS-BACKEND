package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.presentation.PresentationRequest;
import com.sergiocodev.app.dto.presentation.PresentationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PresentationService {
    PresentationResponse create(PresentationRequest request);

    List<PresentationResponse> getAll();

    Page<PresentationResponse> findAllPaged(String description, Pageable pageable);

    PresentationResponse getById(Long id);

    PresentationResponse update(Long id, PresentationRequest request);

    void delete(Long id);
}
