package com.sergiocodev.app.dto.product;

import java.util.List;

public record ProductLookupResponse(
        Long id,
        String code,
        String tradeName,
        List<Long> therapeuticActionIds) {
    public ProductLookupResponse(Long id, String code, String tradeName) {
        this(id, code, tradeName, List.of());
    }
}
