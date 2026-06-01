package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@org.hibernate.annotations.SQLDelete(sql = "UPDATE sale_items SET deleted_at = NOW() WHERE id = ?")
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_unit_id", nullable = false)
    private ProductUnit productUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private ProductLot lot;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "applied_tax_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal appliedTaxRate = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_reason", length = 100)
    private String discountReason;

    @Column(name = "increase_amount", precision = 12, scale = 2)
    private BigDecimal increaseAmount = BigDecimal.ZERO;

    @Column(name = "increase_reason", length = 100)
    private String increaseReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
