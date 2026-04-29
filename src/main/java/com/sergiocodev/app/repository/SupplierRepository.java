package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de proveedores de la botica.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    /**
     * Busca un proveedor por su número de RUC.
     */
    Optional<Supplier> findByRuc(String ruc);
}
