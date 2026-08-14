package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para los tipos de impuestos aplicables (IGV, Exonerado, etc.).
 */
@Repository
public interface TaxTypeRepository extends JpaRepository<TaxType, Long> {
    /**
     * Busca un tipo de impuesto por su nombre.
     */
    Optional<TaxType> findByName(String name);
}
