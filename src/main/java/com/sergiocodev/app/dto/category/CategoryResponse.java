package com.sergiocodev.app.dto.category;

public record CategoryResponse(
        Long id,
        String name,
        String prefix) {
    public CategoryResponse(Long id, String name) {
        this(id, name, null);
    }
}
