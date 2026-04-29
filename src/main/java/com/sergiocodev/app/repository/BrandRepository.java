package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
