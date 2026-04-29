package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Laboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para los laboratorios fabricantes de productos farmacéuticos.
 */
@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    /**
     * Búsqueda por nombre ignorando mayúsculas/minúsculas.
     */
    List<Laboratory> findByNameContainingIgnoreCase(String name);
}
