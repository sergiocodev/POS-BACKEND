package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface LaboratoryService {
    LaboratoryResponse createNewLaboratory(LaboratoryRequest request);

    List<LaboratoryResponse> getAllLaboratory();

    Page<LaboratoryResponse> findAllPaged(String name, Pageable pageable);

    LaboratoryResponse getLaboratoryById(Long id);

    LaboratoryResponse updateLaboratoryById(Long id, LaboratoryRequest request);

    void deleteLaboratoryById(Long id);
}
