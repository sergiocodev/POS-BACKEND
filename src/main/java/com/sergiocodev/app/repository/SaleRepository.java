package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "items", "payments", "customer",
                        "establishment", "user" })
        List<Sale> findByCustomerId(Long customerId);

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "items", "payments", "customer",
                        "establishment", "user" })
        List<Sale> findByCashSessionId(Long cashSessionId);

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "items", "payments", "customer",
                        "establishment", "user" })
        List<Sale> findAll();

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "items", "items.productLot",
                        "items.productLot.product", "establishment", "customer" })
        java.util.Optional<Sale> findWithItemsById(Long id);
}
