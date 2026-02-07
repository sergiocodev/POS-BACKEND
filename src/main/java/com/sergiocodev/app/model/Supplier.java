package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "suppliers")
@Data
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

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
}
