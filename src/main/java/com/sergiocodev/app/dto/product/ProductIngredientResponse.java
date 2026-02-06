package com.sergiocodev.app.dto.product;

public record ProductIngredientResponse(
        Long activeIngredientId,
        String activeIngredientName,
        String concentration) {
}
