package com.sergiocodev.app.dto.documentsequence;

import com.sergiocodev.app.model.DocumentSequence;
import java.time.LocalDateTime;

public record DocumentSequenceResponse(
        Long id,
        Long establishmentId,
        String establishmentName,
        DocumentSequence.DocumentType documentType,
        String series,
        Integer currentNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
