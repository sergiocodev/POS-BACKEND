package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.PharmaceuticalForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para las formas farmacéuticas (Tableta, Ámpula, Jarabe, etc.).
 */
@Repository
public interface PharmaceuticalFormRepository extends JpaRepository<PharmaceuticalForm, Long> {

    /**
     * Busca una forma farmacéutica por su nombre.
     */
    Optional<PharmaceuticalForm> findByName(String name);

    /**
     * Verifica la existencia de una forma farmacéutica.
     */
    boolean existsByName(String name);
}
