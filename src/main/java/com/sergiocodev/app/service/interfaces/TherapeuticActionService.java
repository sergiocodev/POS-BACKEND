package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionRequest;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TherapeuticActionService {
    List<TherapeuticActionResponse> findAll();

    Page<TherapeuticActionResponse> findAllPaged(String name, Pageable pageable);

    TherapeuticActionResponse findById(Long id);

    TherapeuticActionResponse create(TherapeuticActionRequest request);

    TherapeuticActionResponse update(Long id, TherapeuticActionRequest request);

    void delete(Long id);
}
