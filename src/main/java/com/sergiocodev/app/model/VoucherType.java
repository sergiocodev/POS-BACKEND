package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voucher_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "short_name", length = 10)
    private String shortName;

    @Column(name = "code_sunat", length = 10)
    private String codeSunat;

    @Column(nullable = false)
    private boolean active = true;
}
