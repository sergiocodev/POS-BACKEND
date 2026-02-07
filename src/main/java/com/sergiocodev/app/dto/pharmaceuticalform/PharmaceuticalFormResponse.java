package com.sergiocodev.app.dto.pharmaceuticalform;

public record PharmaceuticalFormResponse(
    Long id,
    String name,
    String description,
    boolean active
) {}
