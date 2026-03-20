package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.documentsequence.DocumentSequenceRequest;
import com.sergiocodev.app.dto.documentsequence.DocumentSequenceResponse;
import com.sergiocodev.app.model.DocumentSequence;
import com.sergiocodev.app.model.Establishment;
import com.sergiocodev.app.repository.DocumentSequenceRepository;
import com.sergiocodev.app.repository.EstablishmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentSequenceServiceImpl implements DocumentSequenceService {

    private final DocumentSequenceRepository sequenceRepository;
    private final EstablishmentRepository establishmentRepository;

    @Override
    @Transactional
    public DocumentSequenceResponse create(DocumentSequenceRequest request) {
        Establishment establishment = establishmentRepository.findById(request.establishmentId())
                .orElseThrow(() -> new RuntimeException("Establishment not found"));

        DocumentSequence sequence = new DocumentSequence();
        sequence.setEstablishment(establishment);
        sequence.setDocumentType(request.documentType());
        sequence.setSeries(request.series());
        sequence.setCurrentNumber(request.currentNumber());
        sequence.setCurrentNumber(request.currentNumber());

        return mapToResponse(sequenceRepository.save(sequence));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentSequenceResponse> getAll() {
        return sequenceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentSequenceResponse getById(Long id) {
        return sequenceRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Sequence not found"));
    }

    @Override
    @Transactional
    public DocumentSequenceResponse update(Long id, DocumentSequenceRequest request) {
        DocumentSequence sequence = sequenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sequence not found"));

        sequence.setSeries(request.series());
        sequence.setCurrentNumber(request.currentNumber());
        sequence.setCurrentNumber(request.currentNumber());

        return mapToResponse(sequenceRepository.save(sequence));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sequenceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public String getNextSequence(Long establishmentId, DocumentSequence.DocumentType documentType, String series) {
        DocumentSequence sequence = sequenceRepository.findForUpdate(establishmentId, documentType, series)
                .orElseThrow(() -> new RuntimeException(
                        "Document Sequence not configured for this establishment: " + series));

        Integer nextNum = sequence.getCurrentNumber();
        sequence.setCurrentNumber(nextNum + 1);
        sequenceRepository.save(sequence);

        return String.format("%08d", nextNum);
    }

    private DocumentSequenceResponse mapToResponse(DocumentSequence entity) {
        return new DocumentSequenceResponse(
                entity.getId(),
                entity.getEstablishment().getId(),
                entity.getEstablishment().getName(),
                entity.getDocumentType(),
                entity.getSeries(),
                entity.getCurrentNumber(),
                null, // createdAt is removed
                null // updatedAt is removed
        );
    }
}
