package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ProductLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLotRepository extends JpaRepository<ProductLot, Long> {
    List<ProductLot> findByProductIdOrderByExpiryDateAsc(Long productId);
}
