package com.sergiocodev.app.dto.voideddocument;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record VoidedDocumentRequest(
        @NotNull(message = "Establishment ID is required") Long establishmentId,

        @NotNull(message = "Issue date is required") LocalDate issueDate,

        @NotEmpty(message = "At least one sale ID is required") List<Long> saleIds,

        String description) {
}
