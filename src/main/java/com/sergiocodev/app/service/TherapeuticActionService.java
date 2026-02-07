package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionRequest;
import com.sergiocodev.app.dto.therapeuticaction.TherapeuticActionResponse;
import java.util.List;

public interface TherapeuticActionService {
    List<TherapeuticActionResponse> findAll();

    TherapeuticActionResponse findById(Long id);

    TherapeuticActionResponse create(TherapeuticActionRequest request);

    TherapeuticActionResponse update(Long id, TherapeuticActionRequest request);

    void delete(Long id);
}
