package com.sergiocodev.app.dto;

import com.sergiocodev.app.model.CashConcept;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CashConceptRequest(
    @NotBlank
    String name,
    
    @NotNull
    CashConcept.ConceptType type
) {}
