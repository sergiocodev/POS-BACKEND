package com.sergiocodev.app.dto.voideddocument;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoidedDocumentRequest {

    @NotNull(message = "Establishment ID is required")
    private Long establishmentId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotEmpty(message = "At least one sale ID is required")
    private List<Long> saleIds;

    private String description;
}
