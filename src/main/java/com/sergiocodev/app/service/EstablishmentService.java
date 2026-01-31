package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.establishment.EstablishmentRequest;
import com.sergiocodev.app.dto.establishment.EstablishmentResponse;
import java.util.List;

public interface EstablishmentService {
    EstablishmentResponse create(EstablishmentRequest request);

    List<EstablishmentResponse> getAll();

    EstablishmentResponse getById(Long id);

    EstablishmentResponse update(Long id, EstablishmentRequest request);

    void delete(Long id);
}
