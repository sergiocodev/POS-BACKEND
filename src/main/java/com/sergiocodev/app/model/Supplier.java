package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE suppliers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 20)
    private String ruc;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SupplierStatus status = SupplierStatus.ACTIVO;

    @Column(precision = 3, scale = 1)
    private java.math.BigDecimal rating = java.math.BigDecimal.ZERO;

    @Column(name = "contact_name", length = 255)
    private String contactName;

    public enum SupplierStatus {
        ACTIVO, EN_EVALUACION, VENCIDO
    }
}
