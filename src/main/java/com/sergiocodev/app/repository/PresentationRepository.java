package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Presentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para las presentaciones comerciales de los productos (Caja x 100, Frasco x 500ml, etc.).
 */
@Repository
public interface PresentationRepository extends JpaRepository<Presentation, Long> {
    /**
     * Busca una presentación por su descripción comercial.
     */
    Optional<Presentation> findByDescription(String description);

    @Query("SELECT p FROM Presentation p WHERE " +
           "(:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Presentation> findAllPaged(
        @Param("description") String description,
        Pageable pageable);
}
