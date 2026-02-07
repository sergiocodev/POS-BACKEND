package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "tax_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE tax_types SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class TaxType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal rate;

    @Column(name = "code_sunat", length = 10)
    private String codeSunat;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
