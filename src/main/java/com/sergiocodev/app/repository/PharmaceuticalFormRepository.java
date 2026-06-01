package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.PharmaceuticalForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT pf FROM PharmaceuticalForm pf WHERE " +
           "(:name IS NULL OR LOWER(pf.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<PharmaceuticalForm> findAllPaged(
        @Param("name") String name,
        Pageable pageable);
}
