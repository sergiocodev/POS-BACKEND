package com.sergiocodev.app.dto.therapeuticaction;

public record TherapeuticActionResponse(
        Long id,
        String name,
        String description,
        boolean active) {
}
