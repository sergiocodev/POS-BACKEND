package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE products SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String code;

    @Column(name = "digemid_code", length = 50)
    private String digemidCode;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "trade_name", nullable = false, length = 255)
    private String tradeName;

    @Column(name = "generic_name", length = 255)
    private String genericName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_id")
    private Laboratory laboratory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id")
    private Presentation presentation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_type_id", nullable = false)
    private TaxType taxType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmaceutical_form_id", nullable = false)
    private PharmaceuticalForm pharmaceuticalForm;

    @Column(name = "requires_prescription", nullable = false)
    private boolean requiresPrescription = false;

    @Column(name = "is_generic", nullable = false)
    private boolean isGeneric = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type")
    private UnitType unitType = UnitType.UNI;

    @Column(name = "purchase_factor")
    private Integer purchaseFactor = 1;

    @Column(name = "fraction_label", length = 50)
    private String fractionLabel;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductIngredient> ingredients = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_therapeutic_actions", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "therapeutic_action_id"))
    private Set<TherapeuticAction> therapeuticActions = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum UnitType {
        UNI, ML, GR, AMPOLLA, TABLETA
    }
}
