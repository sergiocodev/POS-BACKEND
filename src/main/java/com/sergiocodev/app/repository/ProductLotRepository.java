package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.ProductLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de lotes de productos o cargamentos específicos.
 */
@Repository
public interface ProductLotRepository extends JpaRepository<ProductLot, Long> {
    /**
     * Lista los lotes de un producto ordenados por su fecha de vencimiento.
     * Facilita el cumplimiento de la política FEFO (First Expired, First Out).
     */
    List<ProductLot> findByProductIdOrderByExpiryDateAsc(Long productId);

    /**
     * Busca un lote específico por el código de lote impreso por el fabricante.
     */
    java.util.Optional<ProductLot> findByProductIdAndLotCode(Long productId, String lotCode);
}
