package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.documentsequence.DocumentSequenceRequest;
import com.sergiocodev.app.dto.documentsequence.DocumentSequenceResponse;
import com.sergiocodev.app.model.DocumentSequence;

import java.util.List;

public interface DocumentSequenceService {
    DocumentSequenceResponse create(DocumentSequenceRequest request);

    List<DocumentSequenceResponse> getAll();

    DocumentSequenceResponse getById(Long id);

    DocumentSequenceResponse update(Long id, DocumentSequenceRequest request);

    void delete(Long id);

    String getNextSequence(Long establishmentId, DocumentSequence.DocumentType documentType, String series);
}
