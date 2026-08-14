package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.establishment.EstablishmentRequest;
import com.sergiocodev.app.dto.establishment.EstablishmentResponse;
import java.util.List;

public interface EstablishmentService {
    EstablishmentResponse create(EstablishmentRequest request);

    List<EstablishmentResponse> getAll();

    org.springframework.data.domain.Page<EstablishmentResponse> getAllPaged(String name, String codeSunat, org.springframework.data.domain.Pageable pageable);

    EstablishmentResponse getById(Long id);

    EstablishmentResponse update(Long id, EstablishmentRequest request);

    void delete(Long id);
}
