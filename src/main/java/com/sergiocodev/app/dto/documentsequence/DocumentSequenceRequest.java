package com.sergiocodev.app.dto.documentsequence;

import com.sergiocodev.app.model.DocumentSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentSequenceRequest(
                @NotNull(message = "Establishment ID is required") Long establishmentId,
                @NotNull(message = "Document Type is required") DocumentSequence.DocumentType documentType,
                @NotBlank(message = "Series is required") String series,
                @NotNull(message = "Current Number is required") Integer currentNumber) {
}
