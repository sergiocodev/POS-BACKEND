package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Establishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de locales, sucursales o establecimientos.
 */
@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Establishment e WHERE e.deletedAt IS NULL AND (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND (:codeSunat IS NULL OR LOWER(e.codeSunat) LIKE LOWER(CONCAT('%', :codeSunat, '%')))")
    org.springframework.data.domain.Page<Establishment> findAllActiveFiltered(
            @org.springframework.data.repository.query.Param("name") String name,
            @org.springframework.data.repository.query.Param("codeSunat") String codeSunat,
            org.springframework.data.domain.Pageable pageable);
}
