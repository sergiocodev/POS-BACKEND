package com.sergiocodev.app.dto.voideddocument;

import com.sergiocodev.app.model.VoidedDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record VoidedDocumentResponse(
        Long id,
        String establishmentName,
        String userName,
        String ticketSunat,
        String xmlUrl,
        String cdrUrl,
        LocalDate issueDate,
        VoidedDocument.VoidedSunatStatus sunatStatus,
        String sunatDescription,
        LocalDateTime createdAt,
        List<VoidedDocumentItemResponse> items) {
    public VoidedDocumentResponse(VoidedDocument doc) {
        this(
                doc.getId(),
                doc.getEstablishment() != null ? doc.getEstablishment().getName() : null,
                doc.getUser() != null ? doc.getUser().getUsername() : null,
                doc.getTicketSunat(),
                doc.getXmlUrl(),
                doc.getCdrUrl(),
                doc.getIssueDate(),
                doc.getSunatStatus(),
                doc.getSunatDescription(),
                doc.getCreatedAt(),
                doc.getItems() != null
                        ? doc.getItems().stream().map(VoidedDocumentItemResponse::new).collect(Collectors.toList())
                        : List.of());
    }
}
