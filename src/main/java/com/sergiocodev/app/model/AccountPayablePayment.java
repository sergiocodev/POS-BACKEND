package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_payable_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountPayablePayment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_payable_id", nullable = false)
    private AccountPayable accountPayable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_session_id", nullable = true)
    private CashSession cashSession;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum PaymentMethod {
        EFECTIVO, TRANSFERENCIA, YAPE, PLIN, TARJETA
    }
}
