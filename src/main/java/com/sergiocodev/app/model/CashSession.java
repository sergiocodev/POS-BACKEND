package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private User user;

    @Column(name = "opening_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "closing_balance", precision = 12, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "calculated_balance", precision = 12, scale = 2)
    private BigDecimal calculatedBalance;

    @Column(name = "diff_amount", precision = 12, scale = 2)
    private BigDecimal diffAmount;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.OPEN;

    public enum SessionStatus {
        OPEN, CLOSED
    }
}
