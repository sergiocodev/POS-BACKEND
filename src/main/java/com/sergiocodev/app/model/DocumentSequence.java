package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_sequences", uniqueConstraints = @UniqueConstraint(name = "ux_seq_estab_doc_series", columnNames = {
        "establishment_id", "document_type", "series" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE document_sequences SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DocumentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(nullable = false, length = 10)
    private String series;

    @Column(name = "current_number", nullable = false)
    private Integer currentNumber = 1;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum DocumentType {
        TICKET, BOLETA, FACTURA, NOTA_DE_VENTA, NOTA_CREDITO, NOTA_DEBITO, GUIA
    }
}
