package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "items", "supplier", "establishment",
            "user" })
    java.util.List<Purchase> findAll();
}
