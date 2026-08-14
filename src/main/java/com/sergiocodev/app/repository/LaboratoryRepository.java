package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Laboratory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para los laboratorios fabricantes de productos farmacéuticos.
 */
@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    /**
     * Busca un laboratorio por su nombre exacto.
     */
    Optional<Laboratory> findByName(String name);

    /**
     * Búsqueda por nombre ignorando mayúsculas/minúsculas.
     */
    List<Laboratory> findByNameContainingIgnoreCase(String name);

    @Query("SELECT l FROM Laboratory l WHERE " +
           "(:name IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Laboratory> findAllPaged(
        @Param("name") String name,
        Pageable pageable);
}
