package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.presentation.PresentationRequest;
import com.sergiocodev.app.dto.presentation.PresentationResponse;
import java.util.List;

public interface PresentationService {
    PresentationResponse create(PresentationRequest request);

    List<PresentationResponse> getAll();

    PresentationResponse getById(Long id);

    PresentationResponse update(Long id, PresentationRequest request);

    void delete(Long id);
}
