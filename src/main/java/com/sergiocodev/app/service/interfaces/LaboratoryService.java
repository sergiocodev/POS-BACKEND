package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.laboratory.LaboratoryRequest;
import com.sergiocodev.app.dto.laboratory.LaboratoryResponse;
import java.util.List;

public interface LaboratoryService {
    LaboratoryResponse createNewLaboratory(LaboratoryRequest request);

    List<LaboratoryResponse> getAllLaboratory();

    LaboratoryResponse getLaboratoryById(Long id);

    LaboratoryResponse updateLaboratoryById(Long id, LaboratoryRequest request);

    void deleteLaboratoryById(Long id);
}
