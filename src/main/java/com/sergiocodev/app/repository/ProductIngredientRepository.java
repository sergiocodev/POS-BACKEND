package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ProductIngredient;
import com.sergiocodev.app.model.ProductIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, ProductIngredientId> {
    List<ProductIngredient> findByProductId(Long productId);
}
