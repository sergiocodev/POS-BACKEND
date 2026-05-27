package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_establishment_id", nullable = false)
    private Establishment sourceEstablishment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_establishment_id", nullable = false)
    private Establishment targetEstablishment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status = TransferStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatched_by_id")
    private User dispatchedBy;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_id")
    private User receivedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferItem> items = new ArrayList<>();

    public enum TransferStatus {
        PENDING, IN_TRANSIT, COMPLETED, CANCELED
    }
}
