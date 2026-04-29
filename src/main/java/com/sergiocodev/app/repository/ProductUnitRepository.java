package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para las unidades de medida y conversión de productos (Caja, Paquete, Unidad).
 */
@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
    /**
     * Lista todas las unidades de medida configuradas para un producto.
     */
    List<ProductUnit> findByProductId(Long productId);

    /**
     * Busca la unidad asociada a un código de barras específico.
     */
    Optional<ProductUnit> findByBarcode(String barcode);

    /**
     * Encuentra la unidad de medida base (mínima indivisible) del producto.
     */
    Optional<ProductUnit> findByProductIdAndIsBaseUnitTrue(Long productId);
}
