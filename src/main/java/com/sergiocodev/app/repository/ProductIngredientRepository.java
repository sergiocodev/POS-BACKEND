package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ProductIngredient;
import com.sergiocodev.app.model.ProductIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la relación intermedia entre productos y sus principios activos.
 */
@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, ProductIngredientId> {
    /**
     * Lista todos los componentes/ingredientes de un producto.
     */
    List<ProductIngredient> findByProductId(Long productId);
}
