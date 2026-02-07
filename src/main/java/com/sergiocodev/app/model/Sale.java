package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_session_id")
    private CashSession cashSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private SaleDocumentType documentType = SaleDocumentType.BOLETA;

    // Campos para Notas de Crédito/Débito
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_sale_id")
    private Sale relatedSale;

    @Column(name = "note_code", length = 5)
    private String noteCode;

    @Column(name = "note_reason", columnDefinition = "TEXT")
    private String noteReason;

    @Column(nullable = false, length = 10)
    private String series;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "sub_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status = SaleStatus.COMPLETED;

    // SUNAT fields
    @Enumerated(EnumType.STRING)
    @Column(name = "sunat_status", length = 30)
    private SunatStatus sunatStatus = SunatStatus.PENDING;

    @Column(name = "sunat_message", columnDefinition = "TEXT")
    private String sunatMessage;

    @Column(name = "hash_cpe", length = 255)
    private String hashCpe;

    @Column(name = "xml_url", columnDefinition = "TEXT")
    private String xmlUrl;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(name = "cdr_url", columnDefinition = "TEXT")
    private String cdrUrl;

    @Column(name = "sunat_response_json", columnDefinition = "JSON")
    private String sunatResponseJson;

    @Column(name = "sunat_error_code", length = 10)
    private String sunatErrorCode;

    // Campos de invalidación/baja
    @Column(name = "is_voided", nullable = false)
    private boolean isVoided = false;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "void_reason", length = 255)
    private String voidReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SaleItem> items = new HashSet<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SalePayment> payments = new HashSet<>();

    public enum SaleDocumentType {
        TICKET, BOLETA, FACTURA, NOTA_CREDITO, NOTA_DEBITO
    }

    public enum SaleStatus {
        COMPLETED, CANCELED
    }

    public enum SunatStatus {
        PENDING, SENT, ACCEPTED, OBSERVED, REJECTED, VOIDED
    }
}
