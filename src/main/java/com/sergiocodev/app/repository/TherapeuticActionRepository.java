package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.TherapeuticAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para las acciones terapéuticas de los medicamentos (Analgésico, Antibiótico, etc.).
 */
@Repository
public interface TherapeuticActionRepository extends JpaRepository<TherapeuticAction, Long> {

    /**
     * Busca una acción terapéutica por su nombre.
     */
    Optional<TherapeuticAction> findByName(String name);

    /**
     * Verifica la existencia de una acción terapéutica.
     */
    boolean existsByName(String name);

    @Query("SELECT ta FROM TherapeuticAction ta WHERE " +
           "(:name IS NULL OR LOWER(ta.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<TherapeuticAction> findAllPaged(
        @Param("name") String name,
        Pageable pageable);
}
