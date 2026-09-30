package com.sergiocodev.app.service.interfaces;

import org.springframework.data.domain.Page;
import com.sergiocodev.app.dto.maintenance.UnitOfMeasureRequest;
import com.sergiocodev.app.dto.maintenance.UnitOfMeasureResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UnitOfMeasureService {
    UnitOfMeasureResponse create(UnitOfMeasureRequest request);
    UnitOfMeasureResponse update(Long id, UnitOfMeasureRequest request);
    void delete(Long id);
    UnitOfMeasureResponse getById(Long id);
    List<UnitOfMeasureResponse> getAll();
    Page<UnitOfMeasureResponse> getAllPaginated(Pageable pageable, String search);
}
