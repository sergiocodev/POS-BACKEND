package com.sergiocodev.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "token_blacklist", indexes = {
    @Index(name = "idx_token_jti", columnList = "jti"),
    @Index(name = "idx_token_expiry", columnList = "expiryDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti", nullable = false, unique = true, length = 255)
    private String jti;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "blacklisted_at", nullable = false)
    private Instant blacklistedAt = Instant.now();

    public void setExpiryDate(Instant expiryDate) {
        if (this.blacklistedAt != null && expiryDate != null && expiryDate.isBefore(this.blacklistedAt)) {
            throw new IllegalArgumentException("expiryDate debe ser posterior a blacklistedAt");
        }
        this.expiryDate = expiryDate;
    }

    public void setBlacklistedAt(Instant blacklistedAt) {
        if (this.expiryDate != null && blacklistedAt != null && blacklistedAt.isAfter(this.expiryDate)) {
            throw new IllegalArgumentException("blacklistedAt debe ser anterior a expiryDate");
        }
        this.blacklistedAt = blacklistedAt;
    }
}
