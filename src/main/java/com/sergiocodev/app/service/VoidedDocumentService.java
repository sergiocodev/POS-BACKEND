package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.voideddocument.VoidedDocumentRequest;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentResponse;
import com.sergiocodev.app.model.VoidedDocument;

import java.util.List;

public interface VoidedDocumentService {

    VoidedDocumentResponse create(VoidedDocumentRequest request, Long userId);

    List<VoidedDocumentResponse> getAll();

    VoidedDocumentResponse getById(Long id);

    VoidedDocumentResponse updateSunatStatus(Long id, VoidedDocument.VoidedSunatStatus status, String description);

    List<VoidedDocumentResponse> getByEstablishment(Long establishmentId);
}
