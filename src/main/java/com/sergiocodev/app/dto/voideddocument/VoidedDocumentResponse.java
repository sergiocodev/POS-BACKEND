package com.sergiocodev.app.dto.voideddocument;

import com.sergiocodev.app.model.VoidedDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoidedDocumentResponse {

    private Long id;
    private String establishmentName;
    private String userName;
    private String ticketSunat;
    private String xmlUrl;
    private String cdrUrl;
    private LocalDate issueDate;
    private VoidedDocument.VoidedSunatStatus sunatStatus;
    private String sunatDescription;
    private LocalDateTime createdAt;
    private List<VoidedDocumentItemResponse> items;

    public VoidedDocumentResponse(VoidedDocument doc) {
        this.id = doc.getId();
        this.establishmentName = doc.getEstablishment() != null ? doc.getEstablishment().getName() : null;
        this.userName = doc.getUser() != null ? doc.getUser().getUsername() : null;
        this.ticketSunat = doc.getTicketSunat();
        this.xmlUrl = doc.getXmlUrl();
        this.cdrUrl = doc.getCdrUrl();
        this.issueDate = doc.getIssueDate();
        this.sunatStatus = doc.getSunatStatus();
        this.sunatDescription = doc.getSunatDescription();
        this.createdAt = doc.getCreatedAt();
        this.items = doc.getItems() != null
                ? doc.getItems().stream().map(VoidedDocumentItemResponse::new).collect(Collectors.toList())
                : List.of();
    }
}
