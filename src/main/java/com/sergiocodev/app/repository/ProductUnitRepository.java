package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
    List<ProductUnit> findByProductId(Long productId);

    Optional<ProductUnit> findByBarcode(String barcode);

    Optional<ProductUnit> findByProductIdAndIsBaseUnitTrue(Long productId);
}
