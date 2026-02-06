package com.sergiocodev.app.dto.activeingredient;

public record ActiveIngredientResponse(
        Long id,
        String name,
        String description,
        boolean active) {
}
