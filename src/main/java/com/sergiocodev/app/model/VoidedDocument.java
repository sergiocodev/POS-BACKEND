package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "voided_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoidedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ticket_sunat", length = 50)
    private String ticketSunat;

    @Column(name = "xml_url", columnDefinition = "TEXT")
    private String xmlUrl;

    @Column(name = "cdr_url", columnDefinition = "TEXT")
    private String cdrUrl;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "sunat_status", length = 30)
    private VoidedSunatStatus sunatStatus = VoidedSunatStatus.PENDING;

    @Column(name = "sunat_description", columnDefinition = "TEXT")
    private String sunatDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "voidedDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoidedDocumentItem> items = new ArrayList<>();

    public enum VoidedSunatStatus {
        PENDING, SENT, ACCEPTED, REJECTED
    }
}
