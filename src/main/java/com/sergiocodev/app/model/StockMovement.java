package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_movement_establishment", columnList = "establishment_id"),
    @Index(name = "idx_movement_lot", columnList = "lot_id"),
    @Index(name = "idx_movement_created", columnList = "created_at"),
    @Index(name = "idx_movement_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private ProductLot lot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "reference_table", length = 50)
    private String referenceTable;

    @Column(name = "reference_id")
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MovementType {
        SALE,
        PURCHASE,
        ADJUSTMENT_IN,
        ADJUSTMENT_OUT,
        ADJUSTMENT,
        TRANSFER_IN,
        TRANSFER_OUT,
        TRANSFER,
        SALE_RETURN,
        REVERSAL,
        VOID_RETURN
    }
}
