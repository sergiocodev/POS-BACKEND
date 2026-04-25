package com.sergiocodev.app.dto;

import com.sergiocodev.app.model.CashConcept;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CashConceptRequest {
    @NotBlank
    private String name;
    
    @NotNull
    private CashConcept.ConceptType type;
}
