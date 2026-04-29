package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para los tipos de impuestos aplicables (IGV, Exonerado, etc.).
 */
@Repository
public interface TaxTypeRepository extends JpaRepository<TaxType, Long> {
}
