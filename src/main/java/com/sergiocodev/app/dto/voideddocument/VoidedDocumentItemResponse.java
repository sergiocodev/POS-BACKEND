package com.sergiocodev.app.dto.voideddocument;

import com.sergiocodev.app.model.VoidedDocumentItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoidedDocumentItemResponse {

    private Long id;
    private Long saleId;
    private String saleDocument;
    private String description;

    public VoidedDocumentItemResponse(VoidedDocumentItem item) {
        this.id = item.getId();
        this.saleId = item.getSale() != null ? item.getSale().getId() : null;
        this.saleDocument = item.getSale() != null
                ? item.getSale().getSeries() + "-" + item.getSale().getNumber()
                : null;
        this.description = item.getDescription();
    }
}
