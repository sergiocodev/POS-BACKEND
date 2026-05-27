package com.sergiocodev.app.dto.voideddocument;

import com.sergiocodev.app.model.VoidedDocumentItem;

public record VoidedDocumentItemResponse(
    Long id,
    Long saleId,
    String saleDocument,
    String description
) {
    public VoidedDocumentItemResponse(VoidedDocumentItem item) {
        this(
            item.getId(),
            item.getSale() != null ? item.getSale().getId() : null,
            item.getSale() != null ? item.getSale().getSeries() + "-" + item.getSale().getNumber() : null,
            item.getDescription()
        );
    }
}
