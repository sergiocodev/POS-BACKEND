package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.documentsequence.DocumentSequenceRequest;
import com.sergiocodev.app.dto.documentsequence.DocumentSequenceResponse;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.model.DocumentSequence;
import com.sergiocodev.app.model.Establishment;
import com.sergiocodev.app.repository.DocumentSequenceRepository;
import com.sergiocodev.app.repository.EstablishmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentSequenceServiceImpl implements DocumentSequenceService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSequenceServiceImpl.class);

    private final DocumentSequenceRepository sequenceRepository;
    private final EstablishmentRepository establishmentRepository;

    @Override
    @Transactional
    public DocumentSequenceResponse create(DocumentSequenceRequest request) {
        Establishment establishment = establishmentRepository.findById(request.establishmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Establishment not found: " + request.establishmentId()));

        // Check for duplicate sequence
        boolean exists = sequenceRepository.existsByEstablishmentIdAndDocumentTypeAndSeries(
                request.establishmentId(), request.documentType(), request.series());
        if (exists) {
            throw new BadRequestException(String.format(
                    "Document sequence already exists for establishment %d, type %s, series %s",
                    request.establishmentId(), request.documentType(), request.series()));
        }

        DocumentSequence sequence = new DocumentSequence();
        sequence.setEstablishment(establishment);
        sequence.setDocumentType(request.documentType());
        sequence.setSeries(request.series());
        sequence.setCurrentNumber(request.currentNumber());

        log.info("Created document sequence: type={}, series={}, initialNumber={} for establishment={}",
                request.documentType(), request.series(), request.currentNumber(), request.establishmentId());

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
                .orElseThrow(() -> new ResourceNotFoundException("Document sequence not found: " + id));
    }

    @Override
    @Transactional
    public DocumentSequenceResponse update(Long id, DocumentSequenceRequest request) {
        DocumentSequence sequence = sequenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document sequence not found: " + id));

        // Check for duplicate if changing type or series
        boolean duplicateExists = sequenceRepository.existsByEstablishmentIdAndDocumentTypeAndSeriesAndIdNot(
                request.establishmentId(), request.documentType(), request.series(), id);
        if (duplicateExists) {
            throw new BadRequestException("Another document sequence with the same type and series already exists");
        }

        sequence.setSeries(request.series());
        sequence.setCurrentNumber(request.currentNumber());

        log.info("Updated document sequence: id={}, series={}, number={}", id, request.series(), request.currentNumber());

        return mapToResponse(sequenceRepository.save(sequence));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!sequenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document sequence not found: " + id);
        }
        sequenceRepository.deleteById(id);
        log.info("Deleted document sequence: id={}", id);
    }

    @Override
    @Transactional
    public String getNextSequence(Long establishmentId, DocumentSequence.DocumentType documentType, String series) {
        DocumentSequence sequence = sequenceRepository.findForUpdate(establishmentId, documentType, series)
                .orElseThrow(() -> new RuntimeException(
                        "Document Sequence not configured for this establishment: " + series));

        Integer currentNum = sequence.getCurrentNumber();
        sequence.setCurrentNumber(currentNum + 1);
        sequenceRepository.save(sequence);

        log.debug("Next sequence: type={}, series={}, number={}", documentType, series, currentNum);
        return String.format("%08d", currentNum);
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
