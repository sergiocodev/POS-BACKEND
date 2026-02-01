package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
        Optional<Product> findByCode(String code);

        // Search for POS: Barcode, Name, or Active Ingredient Name
        // Search for POS: Barcode, Name, or Active Ingredient Name
        @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p LEFT JOIN p.ingredients pi LEFT JOIN pi.activeIngredient ai WHERE "
                        +
                        "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(ai.name) LIKE LOWER(CONCAT('%', :query, '%'))")
        java.util.List<Product> searchByQuery(@org.springframework.data.repository.query.Param("query") String query);

        // Filter by category, brand, and active status
        @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE " +
                        "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                        "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
                        "(:active IS NULL OR p.active = :active)")
        java.util.List<Product> findAllWithFilters(
                        @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
                        @org.springframework.data.repository.query.Param("brandId") Long brandId,
                        @org.springframework.data.repository.query.Param("active") Boolean active);
}
