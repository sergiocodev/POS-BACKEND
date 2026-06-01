package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de marcas de productos.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Busca una marca por su nombre único.
     * @param name Nombre de la marca.
     * @return Optional con la marca.
     */
    Optional<Brand> findByName(String name);

    /**
     * Comprueba si una marca ya está registrada.
     * @param name Nombre a buscar.
     * @return true si ya existe.
     */
    boolean existsByName(String name);

    @Query("SELECT b FROM Brand b WHERE " +
           "(:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Brand> findAllPaged(
        @Param("name") String name, 
        Pageable pageable);
}
